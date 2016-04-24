package zombieSimulator;

import java.util.ArrayList;
import java.util.List;

import org.antlr.works.menu.ContextualMenuFactory;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class Zombie {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int speed;
	private boolean moved = false;
	private int infected;
	private int zombieID;
	
	public int feriocity = 100; //more feriocity = more dangerous in fight, it gets lower as time passes

	private List<GridPoint> previousPoints = new ArrayList<>();
	private int movesRepeated;

	public Zombie(ContinuousSpace<Object> space, Grid<Object> grid, int speed) {
		this.space = space;
		this.grid = grid;
		this.speed = speed;
		this.infected = 0;
		this.zombieID = ZombiesSimulatorBuilder.zombieId++;
	}

	@ScheduledMethod(start = 1, interval = 25)
	public void dropFeriocityWithTime() {
		this.feriocity--;
	}
	
	@ScheduledMethod(start = 1, interval = 5)
	public void takeStep() {
		// pozycja na siatce
		GridPoint point = grid.getLocation(this);

		// stworzymy gridCelle okre�laj�ce pobliskie otoczenie danego zombie
		// dok�adnie 8 otaczaj�cych go kratek, razem ze wsp�rz�dnymi i ilo�ci�
		// ludzi wewn�trz
		GridCellNgh<Human> nghCreator = new GridCellNgh<Human>(grid, point,
				Human.class, 1, 1);

		// poprzez specyfikacjie klasy Human dla listy, przefiltorowujemy
		// istniej�ce tam Zombie
		// warto�� parametru true okre�la aby umie�ci� w liscie tak�e aktualny
		// cell w ktorym zombie si� znajduje
		List<GridCell<Human>> gridCells = nghCreator.getNeighborhood(true);
		// shuffle przemiesza list� by w przypadku gdy wsz�dzie jest taka sama
		// ilosc ludzi nie szedl zawsze w jendym kierunku
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());

		GridPoint whereMostHumansAre = null;
		int maxCount = 0;
		for (GridCell<Human> cell : gridCells) {
			if (cell.size() >= maxCount) {
				whereMostHumansAre = cell.getPoint();

				maxCount = cell.size();
			}
		}

		previousPoints.add(whereMostHumansAre);
		int lastIndex = previousPoints.size() - 1;
		if (previousPoints.size() > 4) {
			if (samePoints(previousPoints.get(lastIndex),
					previousPoints.get(lastIndex - 2))
					&& samePoints(previousPoints.get(lastIndex - 1),
							previousPoints.get(lastIndex - 3))) { // zombie is stack between 2 points
				whereMostHumansAre = new GridPoint(
						whereMostHumansAre.getX() + 1,
						whereMostHumansAre.getY() - 2);
			}
		}

		moveTo(whereMostHumansAre);
		infect();
	}

	private boolean samePoints(GridPoint point1, GridPoint point2) {
		if (point1.getX() == point2.getX() || point1.getY() == point2.getY()) {
			return true;
		}

		return false;
	}

	public void moveTo(GridPoint targetPoint) {
		if (targetPoint.equals(grid.getLocation(this))) {
			return;
		} else { // idziemy w tym kierunku tylko je�li nie jest to ju� nasz
					// aktualna lokacja (kom�rka grid'a)
			NdPoint currentPoint = space.getLocation(this);
			NdPoint toPoint = new NdPoint(targetPoint.getX(),
					targetPoint.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space,
					currentPoint, toPoint);
			space.moveByVector(this, speed, angle, 0);
			currentPoint = space.getLocation(this);

			grid.moveTo(this, (int) currentPoint.getX(),
					(int) currentPoint.getY());
			moved = true;
		}
	}

	@SuppressWarnings("unchecked")
	public void infect() {
		GridPoint point = grid.getLocation(this);
		List<Object> people = new ArrayList<Object>();
		for (Object obj : grid.getObjectsAt(point.getX(), point.getY())) {
			if (obj instanceof Human) {
				if (humanWon((Human) obj) == 0) {
					Context<Object> context = ContextUtils.getContext(this);
					context.remove(this);
					return;
				} else if(humanWon((Human) obj) == 2) {
					Context<Object> context = ContextUtils.getContext(this);
					context.remove(this);
					NdPoint spacePoint = space.getLocation(obj);
					Context<Object> context2 = ContextUtils.getContext(obj);
					context2.remove(obj);
					Zombie zombie = new Zombie(space, grid, speed);
					context2.add(zombie);
					space.moveTo(zombie, spacePoint.getX(), spacePoint.getY());
					grid.moveTo(zombie, (int) spacePoint.getX(),
							(int) spacePoint.getY());
				}
				people.add(obj);
			}
		}

		if (people.size() > 0) {
			int idx = RandomHelper.nextIntFromTo(0, people.size() - 1);
			Object humanObj = people.get(idx);
			NdPoint spacePoint = space.getLocation(humanObj);
			Context<Object> context = ContextUtils.getContext(humanObj);
			context.remove(humanObj);
			Zombie zombie = new Zombie(space, grid, speed);
			this.infected++;
			// System.out.println("New infected at tick "+
			// RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
			context.add(zombie);
			space.moveTo(zombie, spacePoint.getX(), spacePoint.getY());
			grid.moveTo(zombie, (int) spacePoint.getX(),
					(int) spacePoint.getY());
			Network<Object> network = (Network<Object>) context
					.getProjection("infection network");
			network.addEdge(this, zombie);
		}

	}

	private int humanWon(Human human) {
//		if (RandomHelper.nextIntFromTo(1, 100) < human.fightAbility) {
//			return true;
//		}
		return FightSimulator.fight(human, this);
	}

	public int getInfectedCount() {
		return infected;
	}

	public int getZombieId() {
		return zombieID;
	}

}

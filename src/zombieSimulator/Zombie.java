package zombieSimulator;

import java.util.ArrayList;
import java.util.List;

import org.antlr.works.menu.ContextualMenuFactory;

import repast.simphony.context.Context;
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
	private boolean moved = false;
	
	public Zombie(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void takeStep() {
		//pozycja na siatce
		GridPoint point = grid.getLocation(this);
		
		//stworzymy gridCelle okre�laj�ce pobliskie otoczenie danego zombie
		//dok�adnie 8 otaczaj�cych go kratek, razem ze wsp�rz�dnymi i ilo�ci� ludzi wewn�trz
		GridCellNgh<Human> nghCreator = new GridCellNgh<Human>(grid, point, Human.class, 1,1);
		
		//poprzez specyfikacjie kalsy Human dla listy, przefiltorowujemy istniej�ce tam Zombie
		//warto�� parametru true okre�la aby umie�ci� w liscie tak�e aktualny cell w ktorym zombie si� znajduje
		List<GridCell<Human>> gridCells = nghCreator.getNeighborhood(true);
		//shuffle przemiesza list� by w przypadku gdy wsz�dzie jest taka sama ilosc ludzi nie szedl zawsze w jendym kierunku
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		GridPoint whereMostHumansAre = null;
		int maxCount = -1;
		for(GridCell<Human> cell : gridCells) {
			if(cell.size() > maxCount) {
				whereMostHumansAre  = cell.getPoint();
				maxCount = cell.size();
			}
		}
		moveTo(whereMostHumansAre);
	}
	
	public void moveTo(GridPoint targetPoint) {
		if(targetPoint.equals(grid.getLocation(this))) {
			return;
		} else { //idziemy w tym kierunku tylko je�li nie jest to ju� nasz aktualna lokacja (kom�rka grid'a)
			NdPoint currentPoint = space.getLocation(this);
			NdPoint toPoint = new NdPoint(targetPoint.getX(), targetPoint.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, currentPoint, toPoint);
			space.moveByVector(this, 1, angle, 0);
			currentPoint = space.getLocation(this);
			grid.moveTo(this, (int)currentPoint.getX(), (int)currentPoint.getY());
			moved = true;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void infect() {
		GridPoint point = grid.getLocation(this);
		List<Object> people = new ArrayList<Object>();
		for(Object obj : grid.getObjectsAt(point.getX(), point.getY())) {
			if(obj instanceof Human) {
				people.add(obj);
			}
		}
		
		if(people.size() > 0) {
			int idx = RandomHelper.nextIntFromTo(0, people.size() -1);
			Object humanObj = people.get(idx);
			NdPoint spacePoint = space.getLocation(humanObj);
			Context<Object> context = ContextUtils.getContext(humanObj);
			context.remove(humanObj);
			Zombie zombie = new Zombie(space, grid);
			context.add(zombie);
			space.moveTo(zombie,  spacePoint.getX(), spacePoint.getY());
			grid.moveTo(zombie, (int)spacePoint.getX(), (int)spacePoint.getY());
			Network<Object> network = (Network<Object>) context.getProjection("infection network");
			network.addEdge(this, zombie);
		}
		
	}
}

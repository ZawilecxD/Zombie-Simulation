package zombieSimulator;

import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;

public class Human {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int stamina;
	private int speed;
	private int startingStamina;
	
	private boolean alreadyInGroup;
	private boolean escapeNow;
	
	public int fightAbility;
	
	public Human(ContinuousSpace<Object> space, Grid<Object> grid, int startingEnergy, int speed) {
		this.space = space;
		this.grid = grid;
		this.speed = speed;
		this.stamina = startingEnergy;
		this.startingStamina = startingEnergy;
		this.fightAbility = RandomHelper.nextIntFromTo(1, 50);
	}
	
	@ScheduledMethod(start = 1, interval = 3)  //people group up
	public void takeStep() {
		if(escapeNow || alreadyInGroup) {
			return;
		}
		GridPoint point = grid.getLocation(this);
		
		GridCellNgh<Human> nghCreator = new GridCellNgh<Human>(grid, point, Human.class, 1,10);
		
		List<GridCell<Human>> gridCells = nghCreator.getNeighborhood(true);
		//SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		GridPoint whereMostHumansAre = null;
		int maxCount = -1;
		for(GridCell<Human> cell : gridCells) {
			if(cell.size() > maxCount) {
				if(cell.size() > ZombiesSimulatorBuilder.maxGroupSize) {
					alreadyInGroup = true;
					fightAbility+=20; //group bonus
				}
				whereMostHumansAre  = cell.getPoint();
				maxCount = cell.size();
			}
		}
		moveTo(whereMostHumansAre, 1); //we just walk
	}
	
	@Watch(watcheeClassName = "zombieSimulator.Zombie", watcheeFieldNames = "moved",
			query = "within_moore 1",
			whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void escapeOrFight() {
		
		if(alreadyInGroup) {
			return;
		}
		escapeNow = true;
		//punkt aktualnej lokacji czlowieka
		GridPoint currentPoint = grid.getLocation(this);
		
		GridCellNgh<Zombie> nghCreator = new GridCellNgh<>(grid, currentPoint, Zombie.class, 1,1);
		List<GridCell<Zombie>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		GridPoint whereLeastZombiesAre = null;
		int minCount = Integer.MAX_VALUE;
		for(GridCell<Zombie> cell : gridCells) {
			if(cell.size() < minCount) {
				whereLeastZombiesAre = cell.getPoint();
				minCount = cell.size();
			}
		}
		
		if(stamina > 0) {
			moveTo(whereLeastZombiesAre, speed); //we run
		} else {
			stamina = startingStamina;
		}
		
		escapeNow = false;
	}
	
	public void moveTo(GridPoint targetPoint, int moveSpeed) {
		if(targetPoint.equals(grid.getLocation(this))) {
			return;
		} else {
			NdPoint currentPoint = space.getLocation(this);
			NdPoint toPoint = new NdPoint(targetPoint.getX(), targetPoint.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, currentPoint, toPoint);
			space.moveByVector(this, moveSpeed, angle, 0); //2 to dystans ruchu (ile unitow pokona)
			currentPoint = space.getLocation(this);
			grid.moveTo(this, (int)currentPoint.getX(), (int)currentPoint.getY());
			stamina--;
		}
	}
	
}

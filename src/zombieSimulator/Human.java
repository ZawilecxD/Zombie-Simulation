package zombieSimulator;

import java.util.List;

import repast.simphony.context.Context;
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
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class Human {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int stamina;
	private int speed;
	private int startingStamina;
	private int startingFightAbility;
	
	public int fightAbility;
	public Weapon weapon = null;
	
	public int hunger; //the bigger the hungrier
	
	public boolean stopMoving;
	
	private GridPoint targetedCamp;
	private int biggestGroupCount = 0;
	
	public Human(ContinuousSpace<Object> space, Grid<Object> grid, int startingEnergy, int speed) {
		this.space = space;
		this.grid = grid;
		this.speed = speed;
		this.stamina = startingEnergy;
		this.startingStamina = startingEnergy;
		this.fightAbility = RandomHelper.nextIntFromTo(1, 50);
		this.startingFightAbility = fightAbility;
		this.stopMoving = false;
	}
	
	
	@ScheduledMethod(start = 1, interval = 5)
	public void hungerGrows() {
		int amount = RandomHelper.nextIntFromTo(1, 5);
		hunger += amount;
	}
	
	@ScheduledMethod(start = 1, interval = 5)
	public void takeStep() {
		
		if(hunger == 100) {
			Context<Object> context = ContextUtils.getContext(this);
			context.remove(this);
			return;
		}
		
		if(hunger > 50) {
			this.fightAbility = (int)Math.ceil(this.fightAbility/2);
		} else {
			this.fightAbility = startingFightAbility;
		}
		
		GridPoint point = grid.getLocation(this);
		
		if(targetedCamp != null) {
			if(grid.getDistance(point, targetedCamp) < 2) {
				stopMoving = true;
			}	
			if(stopMoving) return;
			moveTo(targetedCamp, 1);
		} else if(targetedCamp == null){
			GridCellNgh<HumanCamp> nghCreator = new GridCellNgh<HumanCamp>(grid, point, HumanCamp.class, 50,50);
			List<GridCell<HumanCamp>> gridCells = nghCreator.getNeighborhood(true);
			SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
			
			for(GridCell<HumanCamp> cell : gridCells) {
				if(cell.size() > 0) {
					targetedCamp  = cell.getPoint();
				}
			}
		}
		
		
	}
	
	
	/*@Watch(watcheeClassName = "zombieSimulator.Zombie", watcheeFieldNames = "moved",
			query = "within_moore 1",
			whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void escapeOrFight() {
		
		if(stopMoving) return;
		
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
		
	}*/
	
	public void moveTo(GridPoint targetPoint, int moveSpeed) {
		if(stopMoving) {
			return;
		} else {
			NdPoint currentPoint = space.getLocation(this);
			NdPoint toPoint = new NdPoint(targetPoint.getX(), targetPoint.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, currentPoint, toPoint);
			space.moveByVector(this, moveSpeed, angle, 0); 
			currentPoint = space.getLocation(this);
			grid.moveTo(this, (int)currentPoint.getX(), (int)currentPoint.getY());
			stamina--;
		}
	}
	
	public void feed(int amount) {
		this.hunger -= amount;
	}
	
}

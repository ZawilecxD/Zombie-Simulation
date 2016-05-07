package zombieSimulator;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class HumanCamp {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	public boolean ready = true;
	private ArrayList<Human> humans = new ArrayList<>();
	private ArrayList<Human> defenslessHumans = new ArrayList<>();
	public int chanceToForgeWeapon;
	public int foodCount;
	
	public HumanCamp(ContinuousSpace<Object> space, Grid<Object> grid, int chanceForWeaponForge) {
		this.space = space;
		this.grid = grid;
		this.chanceToForgeWeapon = chanceForWeaponForge;
	}
	
	public void addNewHuman(Human human) {
		this.humans.add(human);
		if(human.weapon == null) {
			defenslessHumans.add(human);
		}
	}
	
	@ScheduledMethod(start = 1, interval = 10)
	public void forgeWeapon() {
		if(defenslessHumans.size() <= 0) {
			return;
		}
		
		int humanIndex = RandomHelper.nextIntFromTo(0, defenslessHumans.size());
		Human chosen = defenslessHumans.get(humanIndex);
		chosen.weapon = Weapon.generateWeapon();
		defenslessHumans.remove(humanIndex);
	}
	
	@ScheduledMethod(start = 1, interval = 4)
	public void trainPeople() {
		if(humans.size() <= 0) {
			return;
		}
		
		int humanIndex = RandomHelper.nextIntFromTo(0, humans.size());
		Human chosen = humans.get(humanIndex);
		chosen.fightAbility++;
	}
	
	
	@ScheduledMethod(start = 1, interval = 8)
	public void produceFood() {
		if(humans.size() <= 0) {
			return;
		}
		
		int foodAmount = RandomHelper.nextIntFromTo(0, 15);
		foodCount+=foodAmount;
	}
	
	@ScheduledMethod(start = 1, interval = 25)
	public void feedPeople() {
		for(Human h : humans) {
			int amount = (int) Math.ceil(foodCount/humans.size());
			h.feed(amount);
			this.foodCount -= foodCount/humans.size();
		}
		
	}
	
	
}

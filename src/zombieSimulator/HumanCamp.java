package zombieSimulator;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class HumanCamp {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	public boolean ready = true;
	private ArrayList<Human> humans = new ArrayList<>();
	private int humanCount;
	
	public HumanCamp(ContinuousSpace<Object> space, Grid<Object> grid) {
		humanCount = 0;
		this.space = space;
		this.grid = grid;
	}
	
	public void addNewHuman(Human human) {
		System.out.println("ADDED HUMAN");
		this.humans.add(human);
		this.humanCount = humans.size();
	}
	
}

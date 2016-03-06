package zombieSimulator;

import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
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
		
		//stworzymy gridCelle okreœlaj¹ce pobliskie otoczenie danego zombie
		//dok³adnie 8 otaczaj¹cych go kratek, razem ze wspó³rzêdnymi i iloœci¹ ludzi wewn¹trz
		GridCellNgh<Human> nghCreator = new GridCellNgh<Human>(grid, point, Human.class, 1,1);
		
		//poprzez specyfikacjie kalsy Human dla listy, przefiltorowujemy istniej¹ce tam Zombie
		//wartoœæ parametru true okreœla aby umieœciæ w liscie tak¿e aktualny cell w ktorym zombie siê znajduje
		List<GridCell<Human>> gridCells = nghCreator.getNeighborhood(true);
		//shuffle przemiesza listê by w przypadku gdy wszêdzie jest taka sama ilosc ludzi nie szedl zawsze w jendym kierunku
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
		} else { //idziemy w tym kierunku tylko jeœli nie jest to ju¿ nasz aktualna lokacja (komórka grid'a)
			NdPoint currentPoint = space.getLocation(this);
			NdPoint toPoint = new NdPoint(targetPoint.getX(), targetPoint.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, currentPoint, toPoint);
			space.moveByVector(this, 1, angle, 0);
			currentPoint = space.getLocation(this);
			grid.moveTo(this, (int)currentPoint.getX(), (int)currentPoint.getY());
			moved = true;
		}
	}
}

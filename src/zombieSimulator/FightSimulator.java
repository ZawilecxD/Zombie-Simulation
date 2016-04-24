package zombieSimulator;

import repast.simphony.random.RandomHelper;

public class FightSimulator {

	//answers question if human won or lost or it was a "draw"
	//"draw" means that human killed zombie but got infected in process
	// 0 - human won, 1 - zombie won, 2 - draw
	public static int fight(Human human, Zombie zombie) {
		int humanHP = 100;
		int zombieHP = 100;
		int humanChanceToMiss = 75 - human.fightAbility;
		
		//ranged attacks, before zombie gets too close
		if(human.weapon != null && human.weapon.ranged) {
			int attacksNumber = (int)Math.floor(human.fightAbility / 15);
			for(int i=0; i<=attacksNumber;i++) {
				zombieHP -= human.weapon.dmg;
			}
		}
		
		//close combat
		while(humanHP >0 && zombieHP>0) {
			if(RandomHelper.nextIntFromTo(1, 100) < humanChanceToMiss) {
				humanHP -= zombie.feriocity;
			} else {
				if(human.weapon != null) {
					zombieHP -= human.weapon.dmg;
				} else {
					zombieHP -= ZombiesSimulatorBuilder.humanBaseDmg;
				}
				
			}
		}
		
		if(humanHP <= 0 && zombieHP <= 0) {
			return 2;
		} else if(humanHP <= 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
}

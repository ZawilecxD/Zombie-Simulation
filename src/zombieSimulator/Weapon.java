package zombieSimulator;

import repast.simphony.random.RandomHelper;

public class Weapon {
	public int dmg;
	public int minimalFightAbility;
	public boolean ranged;
	
	public Weapon(int dmg, int minimalFA, boolean ranged) {
		this.dmg = dmg;
		this.minimalFightAbility = minimalFA;
		this.ranged = ranged;
	}
	
	public static Weapon generateWeapon() {
		int dmg = RandomHelper.nextIntFromTo(11, 30);
		int minFA = RandomHelper.nextIntFromTo(1, 40);
		int ranged = RandomHelper.nextIntFromTo(1,100);
		if(ranged > 70) {
			return new Weapon(dmg, minFA, true);
		} else {
			return new Weapon(dmg, minFA, false);
		}
	}
	
}

package com.mojang.minecraft.entity.model;

public final class ModelManager {

	private HumanoidModel humanoid = new HumanoidModel(0.0F);
	private HumanoidModel humanoidWithArmor = new HumanoidModel(1.0F);
	private CreeperModel creeper = new CreeperModel();
	private SkeletonModel skeleton = new SkeletonModel();
	private ZombieModel zombie = new ZombieModel();
	private AnimalModel pig = new PigModel();
	private AnimalModel sheep = new SheepModel();
	private SpiderModel spider = new SpiderModel();
	private SheepFurModel sheepFur = new SheepFurModel();

	public final Model getModel(String name) {
		return(name.equals("humanoid") ? this.humanoid : (name.equals("humanoid.armor") ? this.humanoidWithArmor : (name.equals("creeper") ? this.creeper : (name.equals("skeleton") ? this.skeleton : (name.equals("zombie") ? this.zombie : (name.equals("pig") ? this.pig : (name.equals("sheep") ? this.sheep : (name.equals("spider") ? this.spider : (name.equals("sheep.fur") ? this.sheepFur : null)))))))));
	}
}

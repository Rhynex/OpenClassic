package ch.spacebase.openclassic.game;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.spacebase.openclassic.api.Client;
import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.Game;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Server;
import ch.spacebase.openclassic.api.asset.AssetManager;
import ch.spacebase.openclassic.api.asset.AssetSource;
import ch.spacebase.openclassic.api.asset.text.YamlFile;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.command.Command;
import ch.spacebase.openclassic.api.command.CommandExecutor;
import ch.spacebase.openclassic.api.command.Sender;
import ch.spacebase.openclassic.api.event.EventManager;
import ch.spacebase.openclassic.api.event.game.CommandNotFoundEvent;
import ch.spacebase.openclassic.api.event.game.PreCommandEvent;
import ch.spacebase.openclassic.api.inventory.ItemStack;
import ch.spacebase.openclassic.api.inventory.recipe.Fuel;
import ch.spacebase.openclassic.api.inventory.recipe.RecipeManager;
import ch.spacebase.openclassic.api.inventory.recipe.ShapedRecipe;
import ch.spacebase.openclassic.api.inventory.recipe.ShapelessRecipe;
import ch.spacebase.openclassic.api.inventory.recipe.SmeltingRecipe;
import ch.spacebase.openclassic.api.item.VanillaItem;
import ch.spacebase.openclassic.api.item.physics.AxePhysics;
import ch.spacebase.openclassic.api.level.generator.Generator;
import ch.spacebase.openclassic.api.pkg.PackageManager;
import ch.spacebase.openclassic.api.plugin.Plugin;
import ch.spacebase.openclassic.api.plugin.PluginManager;
import ch.spacebase.openclassic.api.scheduler.Scheduler;
import ch.spacebase.openclassic.api.translate.Language;
import ch.spacebase.openclassic.api.translate.Translator;
import ch.spacebase.openclassic.game.block.complex.ClassicChest;
import ch.spacebase.openclassic.game.block.complex.ClassicFurnace;
import ch.spacebase.openclassic.game.scheduler.ClassicScheduler;

public abstract class ClassicGame implements Game {

	private final File directory;
	
	private final YamlFile config;
	private final ClassicScheduler scheduler = new ClassicScheduler(this instanceof Client ? "Client" : "Server");
	
	private final RecipeManager recipes = new RecipeManager();
	private final PluginManager pluginManager = new PluginManager();
	private final EventManager eventManager = new EventManager();
	private final AssetManager assets;
	private final PackageManager pkgManager;
	private final Translator translator = new Translator();
	
	private final Map<Command, Plugin> commands = new HashMap<Command, Plugin>();
	private final Map<CommandExecutor, Plugin> executors = new HashMap<CommandExecutor, Plugin>();
	private final Map<String, Generator> generators = new HashMap<String, Generator>();
	
	public ClassicGame(File directory) {
		this.directory = directory;
		if(this instanceof Server) {
			OpenClassic.setServer((Server) this);
		} else {
			OpenClassic.setClient((Client) this);
		}
		
		this.assets = new AssetManager(directory);
		this.config = this.assets.load("config.yml", AssetSource.FILE, YamlFile.class);
		this.translator.register(new Language("English", "/lang/en_US.lang", AssetSource.JAR));
		this.translator.setDefault("English"); 
		this.pkgManager = new PackageManager();
		this.registerRecipes();
		VanillaBlock.FURNACE_EAST.setComplexBlock(ClassicFurnace.class);
		VanillaBlock.FURNACE_WEST.setComplexBlock(ClassicFurnace.class);
		VanillaBlock.FURNACE_NORTH.setComplexBlock(ClassicFurnace.class);
		VanillaBlock.FURNACE_SOUTH.setComplexBlock(ClassicFurnace.class);
		VanillaBlock.BURNING_FURNACE_EAST.setComplexBlock(ClassicFurnace.class);
		VanillaBlock.BURNING_FURNACE_WEST.setComplexBlock(ClassicFurnace.class);
		VanillaBlock.BURNING_FURNACE_NORTH.setComplexBlock(ClassicFurnace.class);
		VanillaBlock.BURNING_FURNACE_SOUTH.setComplexBlock(ClassicFurnace.class);
		VanillaBlock.CHEST_EAST.setComplexBlock(ClassicChest.class);
		VanillaBlock.CHEST_WEST.setComplexBlock(ClassicChest.class);
		VanillaBlock.CHEST_NORTH.setComplexBlock(ClassicChest.class);
		VanillaBlock.CHEST_SOUTH.setComplexBlock(ClassicChest.class);
	}
	
	private void registerRecipes() {
		this.registerCrafting();
		this.registerSmelting();
		this.registerFuels();
	}
	
	private void registerSmelting() {
		this.getRecipeManager().registerSmelting(new SmeltingRecipe(VanillaBlock.IRON_ORE, new ItemStack(VanillaItem.IRON_INGOT)));
		this.getRecipeManager().registerSmelting(new SmeltingRecipe(VanillaBlock.GOLD_ORE, new ItemStack(VanillaItem.GOLD_INGOT)));
		this.getRecipeManager().registerSmelting(new SmeltingRecipe(VanillaBlock.SAND, new ItemStack(VanillaBlock.GLASS)));
		this.getRecipeManager().registerSmelting(new SmeltingRecipe(VanillaItem.RAW_PORK, new ItemStack(VanillaItem.COOKED_PORK)));
		this.getRecipeManager().registerSmelting(new SmeltingRecipe(VanillaBlock.COBBLESTONE, new ItemStack(VanillaBlock.STONE)));
		this.getRecipeManager().registerSmelting(new SmeltingRecipe(VanillaItem.CLAY, new ItemStack(VanillaItem.BRICK)));
	}
	
	private void registerFuels() {
		for(BlockType block : AxePhysics.getBlocks()) {
			this.getRecipeManager().registerFuel(new Fuel(block, 300));
		}
		
		this.getRecipeManager().registerFuel(new Fuel(VanillaItem.STICK, 100));
		this.getRecipeManager().registerFuel(new Fuel(VanillaItem.COAL, 1600));
		this.getRecipeManager().registerFuel(new Fuel(VanillaItem.LAVA_BUCKET, 20000));
	}
	
	private void registerCrafting() {
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaBlock.WORKBENCH), new String[] {
			"XX",
			"XX"
		}, 'X', VanillaBlock.WOOD));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaBlock.BOOKSHELF), new String[] {
			"###",
			"XXX",
			"###"
		}, '#', VanillaBlock.WOOD, 'X', VanillaItem.BOOK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaBlock.CLAY_BLOCK), new String[] {
			"##",
			"##"
		}, '#', VanillaItem.CLAY));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaBlock.BRICK_BLOCK), new String[] {
			"##",
			"##"
		}, '#', VanillaItem.BRICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaBlock.WHITE_CLOTH), new String[] {
			"##",
			"##"
		}, '#', VanillaItem.STRING));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaBlock.TNT), new String[] {
			"X#X",
			"#X#",
			"X#X"
		}, 'X', VanillaItem.GUNPOWDER, '#', VanillaBlock.SAND));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaBlock.SLAB, 3), new String[] {
			"###"
		}, '#', VanillaBlock.COBBLESTONE));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaBlock.WOOD, 4), new String[] {
			"X"
		}, 'X', VanillaBlock.LOG));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaBlock.IRON_BLOCK, 1), new String[] {
			"XXX",
			"XXX",
			"XXX"
		}, 'X', VanillaItem.IRON_INGOT));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaBlock.GOLD_BLOCK, 1), new String[] {
			"XXX",
			"XXX",
			"XXX"
		}, 'X', VanillaItem.GOLD_INGOT));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaBlock.LAPIS_LAZULI_BLOCK, 1), new String[] {
			"XXX",
			"XXX",
			"XXX"
		}, 'X', VanillaItem.LAPIS_LAZULI));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaBlock.DIAMOND_BLOCK, 1), new String[] {
			"XXX",
			"XXX",
			"XXX"
		}, 'X', VanillaItem.DIAMOND));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.PAPER, 3), new String[] {
			"###"
		}, '#', VanillaItem.REEDS));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.BOOK), new String[] {
			"#",
			"#",
			"#"
		}, '#', VanillaItem.PAPER));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.STICK, 4), new String[] {
			"X",
			"X"
		}, 'X', VanillaBlock.WOOD));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.BUCKET), new String[] {
			"X X",
			" X "
		}, 'X', VanillaItem.IRON_INGOT));
		this.registerWool();
		this.registerTools();
	}
	
	private void registerWool() {
		ShapelessRecipe recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.RED_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.RED_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.ORANGE_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.ORANGE_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.YELLOW_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.YELLOW_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.LIME_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.LIME_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.GREEN_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.GREEN_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.AQUA_GREEN_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.AQUA_GREEN_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.LIGHT_BLUE_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.LIGHT_BLUE_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.BLUE_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.LAPIS_LAZULI));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.PURPLE_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.PURPLE_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.MAGENTA_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.MAGENTA_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.PINK_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.PINK_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.BROWN_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.COCOA_BEANS));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.BLACK_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.BLACK_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.DARK_GRAY_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.DARK_GRAY_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaBlock.GRAY_CLOTH));
		recipe.addItem(new ItemStack(VanillaBlock.WHITE_CLOTH));
		recipe.addItem(new ItemStack(VanillaItem.GRAY_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.RED_DYE, 2));
		recipe.addItem(new ItemStack(VanillaBlock.ROSE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.YELLOW_DYE, 2));
		recipe.addItem(new ItemStack(VanillaBlock.DANDELION));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.PINK_DYE, 2));
		recipe.addItem(new ItemStack(VanillaItem.RED_DYE));
		recipe.addItem(new ItemStack(VanillaItem.BONE_MEAL));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.ORANGE_DYE, 2));
		recipe.addItem(new ItemStack(VanillaItem.RED_DYE));
		recipe.addItem(new ItemStack(VanillaItem.YELLOW_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.LIME_DYE, 2));
		recipe.addItem(new ItemStack(VanillaItem.BONE_MEAL));
		recipe.addItem(new ItemStack(VanillaItem.GREEN_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.DARK_GRAY_DYE, 2));
		recipe.addItem(new ItemStack(VanillaItem.BLACK_DYE));
		recipe.addItem(new ItemStack(VanillaItem.BONE_MEAL));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.GRAY_DYE, 2));
		recipe.addItem(new ItemStack(VanillaItem.DARK_GRAY_DYE));
		recipe.addItem(new ItemStack(VanillaItem.BONE_MEAL));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.GRAY_DYE, 3));
		recipe.addItem(new ItemStack(VanillaItem.BLACK_DYE));
		recipe.addItem(new ItemStack(VanillaItem.BONE_MEAL));
		recipe.addItem(new ItemStack(VanillaItem.BONE_MEAL));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.LIGHT_BLUE_DYE, 2));
		recipe.addItem(new ItemStack(VanillaItem.LAPIS_LAZULI));
		recipe.addItem(new ItemStack(VanillaItem.BONE_MEAL));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.AQUA_GREEN_DYE, 2));
		recipe.addItem(new ItemStack(VanillaItem.LAPIS_LAZULI));
		recipe.addItem(new ItemStack(VanillaItem.GREEN_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.PURPLE_DYE, 2));
		recipe.addItem(new ItemStack(VanillaItem.LAPIS_LAZULI));
		recipe.addItem(new ItemStack(VanillaItem.RED_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.MAGENTA_DYE, 2));
		recipe.addItem(new ItemStack(VanillaItem.PURPLE_DYE));
		recipe.addItem(new ItemStack(VanillaItem.PINK_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.MAGENTA_DYE, 3));
		recipe.addItem(new ItemStack(VanillaItem.LAPIS_LAZULI));
		recipe.addItem(new ItemStack(VanillaItem.RED_DYE));
		recipe.addItem(new ItemStack(VanillaItem.PINK_DYE));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.MAGENTA_DYE, 4));
		recipe.addItem(new ItemStack(VanillaItem.LAPIS_LAZULI));
		recipe.addItem(new ItemStack(VanillaItem.RED_DYE));
		recipe.addItem(new ItemStack(VanillaItem.RED_DYE));
		recipe.addItem(new ItemStack(VanillaItem.BONE_MEAL));
		this.getRecipeManager().registerCrafting(recipe);
		recipe = new ShapelessRecipe(new ItemStack(VanillaItem.BONE_MEAL, 3));
		recipe.addItem(new ItemStack(VanillaItem.BONE));
		this.getRecipeManager().registerCrafting(recipe);
		
	}
	
	private void registerTools() {
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.WOODEN_PICKAXE), new String[] {
			"XXX",
			" # ",
			" # "
		}, 'X', VanillaBlock.WOOD, '#', VanillaItem.STICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.WOODEN_AXE), new String[] {
			"XX",
			"X#",
			" #"
		}, 'X', VanillaBlock.WOOD, '#', VanillaItem.STICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.WOODEN_SHOVEL), new String[] {
			"X",
			"#",
			"#"
		}, 'X', VanillaBlock.WOOD, '#', VanillaItem.STICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.STONE_PICKAXE), new String[] {
			"XXX",
			" # ",
			" # "
		}, 'X', VanillaBlock.COBBLESTONE, '#', VanillaItem.STICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.STONE_AXE), new String[] {
			"XX",
			"X#",
			" #"
		}, 'X', VanillaBlock.COBBLESTONE, '#', VanillaItem.STICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.STONE_SHOVEL), new String[] {
			"X",
			"#",
			"#"
		}, 'X', VanillaBlock.COBBLESTONE, '#', VanillaItem.STICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.IRON_PICKAXE), new String[] {
			"XXX",
			" # ",
			" # "
		}, 'X', VanillaItem.IRON_INGOT, '#', VanillaItem.STICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.IRON_AXE), new String[] {
			"XX",
			"X#",
			" #"
		}, 'X', VanillaItem.IRON_INGOT, '#', VanillaItem.STICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.IRON_SHOVEL), new String[] {
			"X",
			"#",
			"#"
		}, 'X', VanillaItem.IRON_INGOT, '#', VanillaItem.STICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.GOLD_PICKAXE), new String[] {
			"XXX",
			" # ",
			" # "
		}, 'X', VanillaItem.GOLD_INGOT, '#', VanillaItem.STICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.GOLD_AXE), new String[] {
			"XX",
			"X#",
			" #"
		}, 'X', VanillaItem.GOLD_INGOT, '#', VanillaItem.STICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.GOLD_SHOVEL), new String[] {
			"X",
			"#",
			"#"
		}, 'X', VanillaItem.GOLD_INGOT, '#', VanillaItem.STICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.DIAMOND_PICKAXE), new String[] {
			"XXX",
			" # ",
			" # "
		}, 'X', VanillaItem.DIAMOND, '#', VanillaItem.STICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.DIAMOND_AXE), new String[] {
			"XX",
			"X#",
			" #"
		}, 'X', VanillaItem.DIAMOND, '#', VanillaItem.STICK));
		this.getRecipeManager().registerCrafting(new ShapedRecipe(new ItemStack(VanillaItem.DIAMOND_SHOVEL), new String[] {
			"X",
			"#",
			"#"
		}, 'X', VanillaItem.DIAMOND, '#', VanillaItem.STICK));
	}
	
	@Override
	public AssetManager getAssetManager() {
		return this.assets;
	}
	
	@Override
	public RecipeManager getRecipeManager() {
		return this.recipes;
	}
	
	@Override
	public PackageManager getPackageManager() {
		return this.pkgManager;
	}

	@Override
	public Scheduler getScheduler() {
		return this.scheduler;
	}

	@Override
	public PluginManager getPluginManager() {
		return this.pluginManager;
	}

	public void registerCommand(Plugin plugin, Command command) {
		this.commands.put(command, plugin);
	}
	
	public void registerExecutor(Plugin plugin, CommandExecutor executor) {
		this.executors.put(executor, plugin);
	}
	
	@Override
	public void unregisterCommands(Plugin plugin) {
		for(Command command : new ArrayList<Command>(this.commands.keySet())) {
			if(this.commands.get(command) != null && this.commands.get(command).getDescription().getName().equals(plugin.getDescription().getName())) {
				this.commands.remove(command);
			}
		}
	}

	@Override
	public void unregisterExecutors(Plugin plugin) {
		for(CommandExecutor executor : new ArrayList<CommandExecutor>(this.executors.keySet())) {
			if(this.executors.get(executor) != null && this.executors.get(executor).getDescription().getName().equals(plugin.getDescription().getName())) {
				this.executors.remove(executor);
			}
		}
	}

	@Override
	public void processCommand(Sender sender, String command) {
		if(command.length() == 0) return;
		PreCommandEvent event = OpenClassic.getGame().getEventManager().dispatch(new PreCommandEvent(sender, command));
		if(event.isCancelled()) {
			return;
		}
		
		String split[] = event.getCommand().split(" ");
		for(CommandExecutor executor : this.executors.keySet()) {
			if(executor.getCommand(split[0]) != null) {
				try {
					Method method = executor.getCommand(split[0]);
					ch.spacebase.openclassic.api.command.annotation.Command annotation = method.getAnnotation(ch.spacebase.openclassic.api.command.annotation.Command.class);
					
					if(annotation.senders().length > 0) {
						boolean match = false;
						
						for(Class<? extends Sender> allowed : annotation.senders()) {
							if(allowed.isAssignableFrom(sender.getClass())) {
								match = true;
							}
						}
						
						if(!match) {
							if(annotation.senders().length == 1) {
								sender.sendMessage(Color.RED + String.format(this.translator.translate("command.wrong-sender.single", sender.getLanguage()), annotation.senders()[0].getSimpleName().toLowerCase()));
							} else {
								sender.sendMessage(Color.RED + String.format(this.translator.translate("command.wrong-sender.multi", sender.getLanguage()), Arrays.toString(annotation.senders()).toLowerCase()));
							}
							
							return;
						}
					}
					
					if(!sender.hasPermission(annotation.permission())) {
						sender.sendMessage(Color.RED + this.translator.translate("command.no-perm", sender.getLanguage()));
						return;
					}
					
					if(split.length - 1 < annotation.min() || split.length - 1 > annotation.max()) {
						sender.sendMessage(Color.RED + this.translator.translate("command.usage", sender.getLanguage()) + ": " + sender.getCommandPrefix() + split[0] + " " + annotation.usage());
						return;
					}
					
					method.invoke(executor, sender, split[0], Arrays.copyOfRange(split, 1, split.length));
				} catch (Exception e) {
					OpenClassic.getLogger().severe(String.format(this.translator.translate("command.fail-invoke"), split[0]));
					e.printStackTrace();
				}
				
				return;
			}
		}
		
		for(Command cmd : this.getCommands()) {
			if(Arrays.asList(cmd.getAliases()).contains(split[0])) {
				if(cmd.getSenders() != null && cmd.getSenders().length > 0) {
					boolean match = false;
					
					for(Class<? extends Sender> allowed : cmd.getSenders()) {
						if(sender.getClass() == allowed) {
							match = true;
						}
					}
					
					if(!match) {
						if(cmd.getSenders().length == 1) {
							sender.sendMessage(Color.RED + String.format(this.translator.translate("command.wrong-sender.single", sender.getLanguage()), cmd.getSenders()[0].getSimpleName().toLowerCase()));
						} else {
							sender.sendMessage(Color.RED + String.format(this.translator.translate("command.wrong-sender.multi", sender.getLanguage()), Arrays.toString(cmd.getSenders()).toLowerCase()));
						}
						return;
					}
				}
				
				if(!sender.hasPermission(cmd.getPermission())) {
					sender.sendMessage(Color.RED + this.translator.translate("command.no-perm", sender.getLanguage()));
					return;
				}
				
				if((split.length - 1) < cmd.getMinArgs() || (split.length - 1) > cmd.getMaxArgs()) {
					sender.sendMessage(Color.RED + this.translator.translate("command.usage", sender.getLanguage()) + ": " + sender.getCommandPrefix() + split[0] + " " + cmd.getUsage());
					return;
				}
				
				cmd.execute(sender, split[0], Arrays.copyOfRange(split, 1, split.length));
				return;
			}
			
			break;
		}
		
		CommandNotFoundEvent e = OpenClassic.getGame().getEventManager().dispatch(new CommandNotFoundEvent(sender, command));
		if(e.showMessage()) {
			sender.sendMessage(Color.RED + this.translator.translate("command.unknown", sender.getLanguage()));
		}
	}

	public Collection<Command> getCommands() {
		return this.commands.keySet();
	}

	@Override
	public Collection<CommandExecutor> getCommandExecutors() {
		return this.executors.keySet();
	}

	@Override
	public YamlFile getConfig() {
		return this.config;
	}

	public void registerGenerator(Generator generator) {
		if(generator == null) return;
		this.generators.put(generator.getName(), generator);
	}
	
	public Generator getGenerator(String name) {
		return this.generators.get(name);
	}
	
	public Map<String, Generator> getGenerators() {
		return new HashMap<String, Generator>(this.generators);
	}
	
	public boolean isGenerator(String name) {
		return this.getGenerator(name) != null;
	}

	@Override
	public File getDirectory() {
		return this.directory;
	}

	@Override
	public void reload() {
		try {
			this.config.save();
			this.config.load();
		} catch(IOException e) {
			OpenClassic.getLogger().severe("Failed to reload config!");
			e.printStackTrace();
		}
		
		for(Plugin plugin : this.pluginManager.getPlugins()) {
			plugin.reload();
		}
	}
	
	@Override
	public EventManager getEventManager() {
		return this.eventManager;
	}
	
	@Override
	public Translator getTranslator() {
		return this.translator;
	}
	
	@Override
	public String getLanguage() {
		return this.config.getString("settings.language", "English");
	}

}

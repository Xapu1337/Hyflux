package io.fabrica.machine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all machine recipes.
 * Provides lookup methods to find recipes by input item or machine type.
 */
public class RecipeRegistry {

    private static final RecipeRegistry INSTANCE = new RecipeRegistry();

    private final Map<String, Recipe> recipesById;
    private final Map<String, List<Recipe>> recipesByMachine;
    private final Map<String, Map<String, Recipe>> recipesByMachineAndInput;

    private RecipeRegistry() {
        this.recipesById = new ConcurrentHashMap<>();
        this.recipesByMachine = new ConcurrentHashMap<>();
        this.recipesByMachineAndInput = new ConcurrentHashMap<>();
    }

    /**
     * Gets the singleton instance.
     */
    public static RecipeRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a recipe.
     *
     * @param recipe the recipe to register
     * @throws IllegalArgumentException if a recipe with the same ID already exists
     */
    public void register(@Nonnull Recipe recipe) {
        if (recipesById.containsKey(recipe.getId())) {
            throw new IllegalArgumentException("Recipe already registered: " + recipe.getId());
        }

        recipesById.put(recipe.getId(), recipe);

        // Index by machine type
        recipesByMachine.computeIfAbsent(recipe.getMachineType(), k -> new ArrayList<>())
                .add(recipe);

        // Index by machine type and input item
        recipesByMachineAndInput
                .computeIfAbsent(recipe.getMachineType(), k -> new ConcurrentHashMap<>())
                .put(recipe.getInputItemId(), recipe);
    }

    /**
     * Unregisters a recipe.
     *
     * @param recipeId the recipe ID to unregister
     * @return the removed recipe, or null if not found
     */
    @Nullable
    public Recipe unregister(@Nonnull String recipeId) {
        Recipe recipe = recipesById.remove(recipeId);
        if (recipe != null) {
            List<Recipe> machineRecipes = recipesByMachine.get(recipe.getMachineType());
            if (machineRecipes != null) {
                machineRecipes.remove(recipe);
            }

            Map<String, Recipe> inputRecipes = recipesByMachineAndInput.get(recipe.getMachineType());
            if (inputRecipes != null) {
                inputRecipes.remove(recipe.getInputItemId());
            }
        }
        return recipe;
    }

    /**
     * Gets a recipe by its ID.
     *
     * @param recipeId the recipe ID
     * @return the recipe, or null if not found
     */
    @Nullable
    public Recipe getById(@Nonnull String recipeId) {
        return recipesById.get(recipeId);
    }

    /**
     * Gets all recipes for a machine type.
     *
     * @param machineType the machine type ID
     * @return list of recipes (empty if none)
     */
    @Nonnull
    public List<Recipe> getRecipesForMachine(@Nonnull String machineType) {
        List<Recipe> recipes = recipesByMachine.get(machineType);
        return recipes != null ? Collections.unmodifiableList(recipes) : Collections.emptyList();
    }

    /**
     * Finds a recipe for a machine type and input item.
     *
     * @param machineType the machine type ID
     * @param inputItemId the input item type ID
     * @return the matching recipe, or null if not found
     */
    @Nullable
    public Recipe findRecipe(@Nonnull String machineType, @Nonnull String inputItemId) {
        Map<String, Recipe> inputRecipes = recipesByMachineAndInput.get(machineType);
        return inputRecipes != null ? inputRecipes.get(inputItemId) : null;
    }

    /**
     * Checks if a recipe exists for the given machine and input.
     *
     * @param machineType the machine type ID
     * @param inputItemId the input item type ID
     * @return true if a recipe exists
     */
    public boolean hasRecipe(@Nonnull String machineType, @Nonnull String inputItemId) {
        return findRecipe(machineType, inputItemId) != null;
    }

    /**
     * Gets all registered recipes.
     *
     * @return unmodifiable collection of all recipes
     */
    @Nonnull
    public Collection<Recipe> getAllRecipes() {
        return Collections.unmodifiableCollection(recipesById.values());
    }

    /**
     * Gets the count of registered recipes.
     */
    public int getRecipeCount() {
        return recipesById.size();
    }

    /**
     * Clears all registered recipes.
     * Primarily for testing purposes.
     */
    public void clear() {
        recipesById.clear();
        recipesByMachine.clear();
        recipesByMachineAndInput.clear();
    }

    /**
     * Registers all default Fabrica recipes.
     * Called during plugin initialization.
     */
    public void registerDefaultRecipes() {
        // Macerator recipes (ore doubling)
        register(Recipe.builder("macerator_iron_ore")
                .input("Iron_Ore")
                .output("Iron_Dust", 2)
                .energy(1600)  // 1,600 J = 10 seconds at 160W
                .machine("macerator")
                .build());

        register(Recipe.builder("macerator_copper_ore")
                .input("Copper_Ore")
                .output("Copper_Dust", 2)
                .energy(1600)
                .machine("macerator")
                .build());

        register(Recipe.builder("macerator_tin_ore")
                .input("Tin_Ore")
                .output("Tin_Dust", 2)
                .energy(1600)
                .machine("macerator")
                .build());

        // Electric Furnace recipes (smelting dusts)
        register(Recipe.builder("furnace_iron_dust")
                .input("Iron_Dust")
                .output("Iron_Ingot")
                .energy(1280)  // 1,280 J = 8 seconds at 160W
                .machine("electric_furnace")
                .build());

        register(Recipe.builder("furnace_copper_dust")
                .input("Copper_Dust")
                .output("Copper_Ingot")
                .energy(1280)
                .machine("electric_furnace")
                .build());

        register(Recipe.builder("furnace_tin_dust")
                .input("Tin_Dust")
                .output("Tin_Ingot")
                .energy(1280)
                .machine("electric_furnace")
                .build());

        // Direct ore smelting (no doubling bonus)
        register(Recipe.builder("furnace_iron_ore")
                .input("Iron_Ore")
                .output("Iron_Ingot")
                .energy(1280)
                .machine("electric_furnace")
                .build());

        register(Recipe.builder("furnace_copper_ore")
                .input("Copper_Ore")
                .output("Copper_Ingot")
                .energy(1280)
                .machine("electric_furnace")
                .build());

        register(Recipe.builder("furnace_tin_ore")
                .input("Tin_Ore")
                .output("Tin_Ingot")
                .energy(1280)
                .machine("electric_furnace")
                .build());
    }
}

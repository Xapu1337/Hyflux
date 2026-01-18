package io.fabrica.transport;

import io.fabrica.api.power.BlockPos;
import io.fabrica.api.power.PowerCapability;
import io.fabrica.machine.ItemStack;
import io.fabrica.machine.MachineBlock;
import io.fabrica.machine.MachineData;
import io.fabrica.machine.MachineRegistry;
import io.fabrica.machine.ProcessingMachine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Conveyor belt that moves items from one location to another.
 *
 * Features:
 * - Directional (N/E/S/W)
 * - Items move at 1 block per second (20 ticks)
 * - No power required (passive)
 * - Transfers items to adjacent machines or conveyors
 */
public class ConveyorBlock extends MachineBlock {

    // Speed: 1 block per second = 1/20 per tick = 0.05 progress per tick
    public static final double SPEED = 0.05;
    public static final int MAX_ITEMS = 4;  // Max items on one belt

    private Direction direction;
    private final List<ConveyorItem> items;

    public ConveyorBlock(BlockPos position, Direction direction) {
        super("conveyor", position);
        this.direction = direction;
        this.items = new ArrayList<>();
    }

    public ConveyorBlock(BlockPos position) {
        this(position, Direction.NORTH);
    }

    @Override
    public PowerCapability getCapability() {
        return PowerCapability.NONE;  // Conveyors don't use power in MVP
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * Rotates the conveyor direction clockwise.
     */
    public void rotate() {
        this.direction = direction.rotateClockwise();
    }

    /**
     * Gets the items currently on this conveyor.
     */
    public List<ConveyorItem> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Checks if this conveyor can accept more items.
     */
    public boolean canAcceptItem() {
        if (items.size() >= MAX_ITEMS) {
            return false;
        }
        // Check if there's space at the start of the belt
        for (ConveyorItem item : items) {
            if (item.getProgress() < 0.25) {
                return false;  // Item too close to start
            }
        }
        return true;
    }

    /**
     * Adds an item to the start of the conveyor.
     *
     * @param stack the item to add
     * @return true if added successfully
     */
    public boolean addItem(ItemStack stack) {
        if (!canAcceptItem() || stack.isEmpty()) {
            return false;
        }
        items.add(new ConveyorItem(stack.copy()));
        return true;
    }

    @Override
    public void tick() {
        if (items.isEmpty()) {
            setActive(false);
            return;
        }

        setActive(true);
        MachineRegistry registry = MachineRegistry.getInstance();
        BlockPos targetPos = direction.offset(position);

        Iterator<ConveyorItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            ConveyorItem item = iterator.next();

            // Try to advance the item
            if (item.advance(SPEED)) {
                // Item reached end of belt, try to transfer
                if (tryTransfer(item, targetPos, registry)) {
                    iterator.remove();
                } else {
                    // Can't transfer, item stays at end
                    item.setProgress(1.0);
                }
            }
        }
    }

    /**
     * Attempts to transfer an item to the target position.
     */
    private boolean tryTransfer(ConveyorItem item, BlockPos targetPos, MachineRegistry registry) {
        MachineBlock target = registry.getMachineAt(targetPos);

        if (target == null) {
            // No machine at target - item would fall off
            // In a real implementation, might spawn item entity
            return false;
        }

        // Try to transfer to another conveyor
        if (target instanceof ConveyorBlock conveyor) {
            return conveyor.addItem(item.getItemStack());
        }

        // Try to transfer to a processing machine
        if (target instanceof ProcessingMachine machine) {
            ItemStack overflow = machine.insertInput(item.getItemStack());
            return overflow.isEmpty();
        }

        // Unknown target type
        return false;
    }

    /**
     * Gets the position this conveyor outputs to.
     */
    public BlockPos getOutputPosition() {
        return direction.offset(position);
    }

    /**
     * Gets the position this conveyor receives input from.
     */
    public BlockPos getInputPosition() {
        return direction.getOpposite().offset(position);
    }

    @Override
    public void onInteract(UUID playerUuid) {
        // Rotate on interact
        rotate();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void saveAdditionalData(MachineData data) {
        data.set("direction", direction.name());
        // Serialize items
        List<String> itemIds = new ArrayList<>();
        List<Integer> itemCounts = new ArrayList<>();
        List<Double> itemProgress = new ArrayList<>();
        for (ConveyorItem item : items) {
            itemIds.add(item.getItemStack().getItemId());
            itemCounts.add(item.getItemStack().getCount());
            itemProgress.add(item.getProgress());
        }
        data.set("itemIds", itemIds);
        data.set("itemCounts", itemCounts);
        data.set("itemProgress", itemProgress);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void loadAdditionalData(MachineData data) {
        String dirName = data.getString("direction");
        try {
            direction = dirName != null ? Direction.valueOf(dirName) : Direction.NORTH;
        } catch (IllegalArgumentException e) {
            direction = Direction.NORTH;
        }

        // Deserialize items
        items.clear();
        List<String> itemIds = data.get("itemIds", List.class);
        List<Integer> itemCounts = data.get("itemCounts", List.class);
        List<Double> itemProgressList = data.get("itemProgress", List.class);

        if (itemIds != null) {
            for (int i = 0; i < itemIds.size(); i++) {
                String id = itemIds.get(i);
                int count = (itemCounts != null && i < itemCounts.size()) ? itemCounts.get(i) : 1;
                double progress = (itemProgressList != null && i < itemProgressList.size()) ? itemProgressList.get(i) : 0.0;
                items.add(new ConveyorItem(ItemStack.of(id, count), progress));
            }
        }
    }

    @Override
    public String getMachineTypeName() {
        return "Conveyor Belt";
    }

    @Override
    public String getMachineTypeId() {
        return "conveyor";
    }

    @Override
    public String toString() {
        return String.format("Conveyor[pos=%s, dir=%s, items=%d]", position, direction, items.size());
    }
}

package com.railwayteam.railways.blocks;

import com.railwayteam.railways.ModSetup;
import com.railwayteam.railways.util.SoundUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class HornBlock extends HorizontalFaceBlock {
    public static class HornItem extends BlockItem {
        ArrayList<UUID> Using = new ArrayList<>();

//        @Override
//        public ActionResultType onItemUse(ItemUseContext ctx) {
//            PlayerEntity plr = ctx.getPlayer();
//            if(plr.isCrouching()) {
//                World world = ctx.getWorld();
//                if(world.isRemote) {
//                    Using.add(plr);
//                    SoundUtil.playSoundUntil(Doot, SoundCategory.PLAYERS, 2, 1, plr.getX(), plr.getY(), plr.getZ(), soundCtx ->
//                            soundCtx.ticks < getUseDuration(ctx.getItem()) &&
//                            Using.contains(plr),
//
//                            (soundCtx) -> Using.remove(plr));
//                }
//            }
//            return super.onItemUse(ctx);
//        }


        @Override
        public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity plr, Hand hand) {
            if(plr.isSneaking() && world.isRemote && !Using.contains(plr.getUniqueID())) {
                plr.setActiveHand(hand); // uh i guess this should work?????
                ItemStack stack = plr.getHeldItem(hand);
                Using.add(plr.getUniqueID());
//                world.playSound(null, plr.getBlockPos(), ModSetup.R_SOUND_DOOT.get(), SoundCategory.PLAYERS, 2, 1);
                SoundUtil.playSoundUntil(ModSetup.R_SOUND_DOOT.get(), SoundCategory.PLAYERS, 2, 1, plr.getX(), plr.getY(), plr.getZ(), soundCtx ->
                            Using.contains(plr.getUniqueID()) &&
                            soundCtx.ticks < getUseDuration(stack),

                            (soundCtx) -> Using.remove(plr));
            }
            return super.onItemRightClick(world,plr,hand);
        }

        public void stopSound(PlayerEntity plr) {
//            World world = plr.world;
            Using.remove(plr.getUniqueID());
        }

        @Override
        public ItemStack onItemUseFinish(ItemStack p_77654_1_, World p_77654_2_, LivingEntity entity) {
            stopSound((PlayerEntity) entity);
            return super.onItemUseFinish(p_77654_1_, p_77654_2_, entity);
        }

        @Override
        public void onPlayerStoppedUsing(ItemStack p_77615_1_, World p_77615_2_, LivingEntity p_77615_3_, int p_77615_4_) {
            stopSound((PlayerEntity) p_77615_3_);
            super.onPlayerStoppedUsing(p_77615_1_, p_77615_2_, p_77615_3_, p_77615_4_);
        }

        @Override
        public int getUseDuration(ItemStack p_77626_1_) {
            return 5 * 20;
        }

        public HornItem(Block p_i48527_1_, Properties p_i48527_2_) {
            super(p_i48527_1_, p_i48527_2_);
        }
    }

    public static final IntegerProperty HORNS = IntegerProperty.create("horns", 1, 3);

    public HornBlock(Properties p_i48402_1_) {
        super(p_i48402_1_);
        this.setDefaultState(this.stateContainer.getBaseState().with(HORIZONTAL_FACING, Direction.NORTH).with(HORNS, 1).with(FACE, AttachFace.WALL));
    }

    // couldnt figure out how to datagen a loot table that uses blockstate properties so ill just hardcode it for now
    // its definetely better to use a json one for datapack compat
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder p_220076_2_) {
        return Collections.singletonList(new ItemStack(this, state.get(HORNS)));
    }

    public void setHorns(World world, BlockPos pos, int horns) {
        world.setBlockState(pos, world.getBlockState(pos).with(HORNS, horns), 1);
//        world.notifyNeighborsOfStateChange(pos, this);
    }

    @Override
    public ActionResultType onUse(BlockState blockState, World world, BlockPos pos, PlayerEntity plr, Hand hand, BlockRayTraceResult raytrace) {
        if(plr.isSneaking() || !plr.isAllowEdit()) return ActionResultType.PASS;
        ItemStack stack = plr.getHeldItem(hand);
        int horns = blockState.get(HORNS);
        if(stack.getItem().getRegistryName().equals(this.getRegistryName()) && horns < 3) {
            setHorns(world, pos,  horns+ 1);
            if(!plr.isCreative()) {
                stack.shrink(1);
            }
        } else {
            // TODO: when the horn sound is done, use that sound here
            world.playSound(null, pos, SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 2.0F, 1.0F);
        }
        return ActionResultType.SUCCESS;
    }

    // shape stuff
    public static VoxelShape ShapeBottomNorthSouth = Block.makeCuboidShape(0, 0, 1, 16, 15, 15);
    public static VoxelShape ShapeBottomWestEast = Block.makeCuboidShape(1, 0, 0, 15, 15, 16);

    public static VoxelShape ShapeSideNorth = Block.makeCuboidShape(0, 1, 1, 16, 15, 16);
    public static VoxelShape ShapeSideSouth = Block.makeCuboidShape(0, 1, 0, 16, 15, 15);
    public static VoxelShape ShapeSideWest = Block.makeCuboidShape(1, 1, 0, 16, 15, 16);
    public static VoxelShape ShapeSideEast = Block.makeCuboidShape(0, 1, 0, 15, 15, 16);

    @Override
    public VoxelShape getShape(BlockState blockState, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        Direction direction = blockState.get(HORIZONTAL_FACING);
        switch(blockState.get(FACE)) {
            case FLOOR:
                return direction.getAxis() == Direction.Axis.X ? ShapeBottomNorthSouth : ShapeBottomWestEast;
            case WALL:
                switch (direction) {
                    case WEST:
                        return ShapeSideWest;
                    case EAST:
                        return ShapeSideEast;
                    case NORTH:
                        return ShapeSideNorth;
                    case SOUTH:
                        return ShapeSideSouth;
                }
            default: return ShapeSideNorth;
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(HORIZONTAL_FACING, HORNS, FACE);
    }
}
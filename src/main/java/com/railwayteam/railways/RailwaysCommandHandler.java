package com.railwayteam.railways;

import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;

public class RailwaysCommandHandler {
  public static void registerCommands (RegisterCommandsEvent rce) {
    rce.getDispatcher().register(Commands.literal("graph")
    .then(Commands.literal("set")
    .then(Commands.argument("position", BlockPosArgument.blockPos())
    .executes(context -> {
      if (context.getSource().getEntity() != null) {
        context.getSource().sendErrorMessage(new StringTextComponent("adding track to Graph..."));
      }
      return Railways.SEGMENT_MANAGER.addTrack(BlockPosArgument.getBlockPos(context, "position"));
    })))
    .then(Commands.literal("get")
    .then(Commands.literal("id")
    .then(Commands.argument("position", BlockPosArgument.blockPos())
    .executes(context -> {
      boolean found = Railways.SEGMENT_MANAGER.containsTrack(BlockPosArgument.getBlockPos(context, "position"));
      if (context.getSource().getEntity() != null) {
        context.getSource().sendErrorMessage(new StringTextComponent("track was " + (found ? "" : "not ") + "found in Graph"));
      }
      return 1;
    })))
    .then(Commands.literal("links")
    .then(Commands.argument("position", BlockPosArgument.blockPos())
    .executes(context -> {
      int links = Railways.SEGMENT_MANAGER.countLinks(BlockPosArgument.getBlockPos(context, "position"));
      if (context.getSource().getEntity() != null) {
        context.getSource().sendErrorMessage(new StringTextComponent("track has " + links + " connection" + (links==1?"":"s")));
      }
      return 1;
    })))
    )
    .executes(context -> {
      context.getSource().sendErrorMessage(new StringTextComponent("usage: graph set|get <BlockPos>"));
      return 1;
    })
    );
  }
}

package com.mojang.brigadier.arguments;
import com.mojang.brigadier.context.CommandContext;
public class StringArgumentType implements ArgumentType<String> { public static StringArgumentType word(){return null;} public static String getString(CommandContext<?> c, String n){return null;} }

package com.mojang.brigadier.builder;
import com.mojang.brigadier.Command;
public abstract class ArgumentBuilder<S, T extends ArgumentBuilder<S, T>> {
 public T then(ArgumentBuilder<S, ?> a){return null;} public T executes(Command<S> c){return null;} }

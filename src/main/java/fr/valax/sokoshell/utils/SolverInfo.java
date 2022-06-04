package fr.valax.sokoshell.utils;

import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.Solver;
import fr.valax.sokoshell.solver.SolverStatus;

import java.util.concurrent.CompletableFuture;

public record SolverInfo(CompletableFuture<SolverStatus> solverFuture, Solver solver, Pack pack, Level level) {

}

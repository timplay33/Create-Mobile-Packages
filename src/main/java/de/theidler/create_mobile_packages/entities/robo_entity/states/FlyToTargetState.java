package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Function;

public class FlyToTargetState implements RoboEntityState {
    @Override
    public void tick(RoboEntity re) {
        BlockPos targetPos = re.getTargetPosition();
        if (targetPos == null)
            return;

        if (re.position().distanceTo(targetPos.getCenter()) <= CMPConfigs.server().beeSpeed.get() / 12.0) {
            if (re.getTargetBlockEntity() != null) {
                re.setState(new LandingPrepareState());
            } else if (re.getTargetPlayer() != null) {
                re.setState(new InteractWithPlayerState());
            }
            re.setTargetVelocity(Vec3.ZERO);
        } else {
            if (re.getTargetPlayer() != null) {
                re.updateDisplay(re.getTargetPlayer());
            }

            double speed = CMPConfigs.server().beeSpeed.get() / 20.0;
            if (re.getPathing()) {
                Pathfinder pathfinder = new Pathfinder();
                Function<BlockPos, Boolean> isWalkable = pos ->
                        re.level().getBlockState(new BlockPos(pos)).isAir();
                List<Node> path = pathfinder.findPath(re.blockPosition(), targetPos, isWalkable);
                if (!path.isEmpty()) {
                    Node nextNode = path.get(0);
                    if (re.position().distanceTo(nextNode.pos.getCenter()) < 0.5 && path.size() > 1)
                        nextNode = path.get(1);

                    Vec3 direction = nextNode.pos.getCenter().subtract(re.position()).normalize();
                    re.setTargetVelocity(direction.scale(speed));
                    if (re.position().distanceTo(targetPos.getCenter()) > 2.5) // entity rotation starts drifting
                        re.lookAtTarget();
                }
            } else {
                Vec3 direction = targetPos.getCenter().subtract(re.position()).normalize();
                re.setTargetVelocity(direction.scale(speed));
                if (re.position().distanceTo(targetPos.getCenter()) > 2.5) // entity rotation starts drifting
                    re.lookAtTarget();
            }
        }
    }

    public static class Node {
        public final BlockPos pos;
        public double gCost = Double.POSITIVE_INFINITY;
        public double hCost = 0;
        public double fCost = 0;
        public Node parent = null;

        public Node(BlockPos pos) {
            this.pos = pos;
        }

        public double distanceTo(Node other) {
            return pos.getCenter().distanceTo(other.pos.getCenter());
        }
    }

    public static class Pathfinder {
        public List<Node> findPath(BlockPos startPos, BlockPos goalPos,
                                   Function<BlockPos, Boolean> isWalkable) {
            Map<BlockPos, Node> nodeMap = new HashMap<>();
            PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
            Set<BlockPos> closedSet = new HashSet<>();

            Node start = getOrCreateNode(startPos, nodeMap);
            Node goal = getOrCreateNode(goalPos, nodeMap);

            start.gCost = 0;
            start.hCost = start.distanceTo(goal);
            start.fCost = start.hCost;

            openSet.add(start);

            int maxIterations = 50000;
            int iterations = 0;

            while (!openSet.isEmpty() && iterations++ < maxIterations) {
                Node current = openSet.poll();

                if (current.equals(goal)) return reconstructPath(current);
                closedSet.add(current.pos);

                for (BlockPos neighborPos : getNeighbors(current)) {
                    if (!isWalkable.apply(neighborPos) || closedSet.contains(neighborPos)) continue;

                    Node neighbor = getOrCreateNode(neighborPos, nodeMap);
                    double tentativeG = current.gCost + 1;

                    if (tentativeG < neighbor.gCost) {
                        neighbor.parent = current;
                        neighbor.gCost = tentativeG;
                        neighbor.hCost = neighbor.distanceTo(goal);
                        neighbor.fCost = neighbor.gCost + neighbor.hCost;

                        openSet.remove(neighbor);
                        openSet.add(neighbor);
                    }
                }
            }

            System.err.println("No path found or iteration limit exceeded.");
            return Collections.emptyList();
        }

        private Node getOrCreateNode(BlockPos pos, Map<BlockPos, Node> nodeMap) {
            return nodeMap.computeIfAbsent(pos, Node::new);
        }

        private static final BlockPos[] DIRECTIONS = BlockPos.betweenClosedStream(-1, -1, -1, 1, 1, 1)
                .filter(pos -> !(pos.getX() == 0 && pos.getY() == 0 && pos.getZ() == 0))
                .map(BlockPos::immutable)
                .toArray(BlockPos[]::new);

        private List<BlockPos> getNeighbors(Node node) {
            BlockPos pos = node.pos;
            List<BlockPos> neighbors = new ArrayList<>(26);
            for (BlockPos offset : DIRECTIONS)
                neighbors.add(node.pos.offset(offset));

            return neighbors;
        }

        private static List<Node> reconstructPath(Node end) {
            List<Node> path = new ArrayList<>();
            for (Node n = end; n != null; n = n.parent) path.add(n);
            Collections.reverse(path);
            return path;
        }
    }
}

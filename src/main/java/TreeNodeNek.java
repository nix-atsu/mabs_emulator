import sun.reflect.generics.tree.Tree;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreeNodeNek implements Iterable<TreeNodeNek> {

  public int option;
  public Boolean resolution;
  public BigDecimal cost;
  public BigDecimal revenue;

//  public BigDecimal subTreeRevenue = zero();
//  public Set<Integer> optimalNodes = new HashSet<>();

  public Map<Integer, BigDecimal> choiceRevenue = new HashMap<>();

  public List<Integer> bestChoices = new ArrayList<>();
  public BigDecimal maxChildRevenue = zero();

  public TreeNodeNek parent;
  public List<TreeNodeNek> children;
  private List<TreeNodeNek> elementsIndex;




  public TreeNodeNek(int option, Boolean resolution, BigDecimal cost, BigDecimal revenue) {
    this.option = option;
    this.resolution = resolution;
    this.cost = cost;
    this.revenue = revenue;
//    this.level = 0;
    this.children = new LinkedList<TreeNodeNek>();
    this.elementsIndex = new LinkedList<TreeNodeNek>();
    this.elementsIndex.add(this);
  }

  public TreeNodeNek addChild(int option, Boolean resolution) {
    return addChild(option, resolution, zero());
  }

  private static BigDecimal zero() {
    return new BigDecimal("0", Main.mc);
  }

  private static BigDecimal one() {
    return new BigDecimal("1", Main.mc);
  }

  public TreeNodeNek addChild(int option, Boolean resolution, BigDecimal cost) {
    return addChild(option, resolution, cost, zero());
  }


  public TreeNodeNek addChild(int option, Boolean resolution, BigDecimal cost, BigDecimal revenue) {
    TreeNodeNek childNode = new TreeNodeNek(option, resolution, cost, revenue);
    childNode.parent = this;
    this.children.add(childNode);
    this.registerChildForSearch(childNode);
    return childNode;
  }

  public int getLevel() {
//    return level;
    if (this.isRoot())
      return 0;
    else
      return parent.getLevel() + 1;
  }

  public boolean isRoot() {
    return parent == null;
  }


  public List<TreeNodeNek> getLeaves() {
    List<TreeNodeNek> leaves = new ArrayList<>();
    for (TreeNodeNek node : this) {
      if (node.isTerminalNode()) {
        leaves.add(node);
      }
    }
    return leaves;
  }

  public boolean isTerminalNode() {
    return option == 0;
  }

  public TreeNodeNek getRoot() {
    if (isRoot()) {
      return this;
    };
    return parent.getRoot();
  }

  public BigDecimal getCost() {
    return cost;
  }

  public boolean isLeaf() {
    return children.size() == 0;
  }

  private void registerChildForSearch(TreeNodeNek node) {
    elementsIndex.add(node);
    if (parent != null)
      parent.registerChildForSearch(node);
  }

  public TreeNodeNek findTreeNode(Comparable cmp) {
    for (TreeNodeNek element : this.elementsIndex) {
      int elOption = element.option;
      boolean elResolution = element.resolution;
      if (cmp.compareTo(elOption) == 0 && cmp.compareTo(elResolution) == 0)
        return element;
    }

    return null;
  }

  @Override
  public String toString() {
    return String.valueOf(option) + String.valueOf(resolution)
        ;
  }

  public String toStringFullOverflow() {
    return "option: " + String.valueOf(option) +
     "resolut: " + String.valueOf(resolution) +
        "; parent: " + parent +
        "; children: " + children.toString()
        ;
  }

  public static void printTree(TreeNodeNek treeRoot) {
    for (TreeNodeNek node : treeRoot) {
      String indent = createIndent(node.getLevel());
      System.out.println(indent + String.valueOf(node.option) + String.valueOf(node.resolution) + "(" + node.cost + ";" + node.revenue + ") : " + node.revenue.subtract(node.cost) + "\n");
    }

  }

  public static void printOptimalTreeInfo(TreeNodeNek treeRoot) {
    for (TreeNodeNek node : treeRoot) {
      String indent = createIndent(node.getLevel());
      System.out.println(indent + String.valueOf(node.option) + String.valueOf(node.resolution) + "(revenue: " + node.revenue + "), optimal options : " + node.bestChoices.toString() + ", choiceRevenues : " + node.choiceRevenue.toString());
    }

  }

  private static String createIndent(int depth) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      sb.append(' ');
      sb.append(' ');
      sb.append(' ');
      sb.append(' ');
    }
    return sb.toString();
  }

  public Iterator<TreeNodeNek> iterator() {
    TreeNodeIter iter = new TreeNodeIter(this);
    return iter;
  }


  public BigDecimal calculateCosts() {
    if (isRoot()) {
      return zero();
    }
    return cost.add(parent.calculateCosts());
  }




  public List<Boolean> getInvestigations(int beta) {
    if (option == 0) {
      System.out.println("ERROR: Tried to encode 0 node !!!!!!!!!!!!!!!");
    }
    List<Boolean> out = new ArrayList<>();

    if (option == beta) {
      if (parent != null) {
        out = parent.getInvestigations(beta);
      }
      out.add(resolution);
      return out;
    }
    return (parent == null) ? out : parent.getInvestigations(beta);
  }

  public BigDecimal calculateProbability(BigDecimal[] betas, BigDecimal qu) {
    if (option == 0) {
      System.out.println("ERROR: Tried to encode 0 node !!!!!!!!!!!!!!!");
    }

    if (parent == null) {
      return one();
    } else {
      BigDecimal probability;
      if (resolution) {
        final BigDecimal q1Beta1 = (one().subtract(qu, Main.mc)).multiply(one().subtract(betas[option - 1], Main.mc), Main.mc);
        probability = qu.multiply(betas[option - 1], Main.mc).add(q1Beta1, Main.mc);
      } else {
        final BigDecimal q1Beta = (one().subtract(qu, Main.mc)).multiply(betas[option - 1], Main.mc);
        final BigDecimal qBeta1 = qu.multiply(one().subtract(betas[option - 1], Main.mc), Main.mc);
        probability = q1Beta.add(qBeta1, Main.mc);
      }
      return parent.calculateProbability(betas, qu).multiply(probability, Main.mc);
    }
  }

  public TreeNodeNek getIthBetaChild(int beta, boolean resolution) {
    for (TreeNodeNek child : children) {
      if (child.option == beta && (child.resolution == resolution)) {
        return child;
      }
    }
    return null;
  }


//    if (option.toString().startsWith(Integer.toString(beta))) {
//      return 1 + (parent == null ? 0 : parent.getNumberOfInvestigations(beta));
//    } else {
//      return parent == null ? 0 : parent.getNumberOfInvestigations(beta);
//    }


}
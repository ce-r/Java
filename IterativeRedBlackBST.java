import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

import java.util.NoSuchElementException;

public class RedBlackBST<Key extends Comparable<Key>, Value> {

    private static final boolean RED   = true;
    private static final boolean BLACK = false;

    private Node root;     // root of the BST

    // BST helper node data type
    private class Node {
        private Key key;           // key
        private Value val;         // associated data
        private Node left, right;  // links to left and right subtrees
        private boolean color;     // color of parent link
        private int size;          // subtree count

        public Node(Key key, Value val, boolean color, int size) {
            this.key = key;
            this.val = val;
            this.color = color;
            this.size = size;
        }
    }

    public RedBlackBST() {
    }

    // is node x red; false if x is null ?
    private boolean isRed(Node x) {
        if (x == null) return false;
        return x.color == RED;
    }

    // number of node in subtree rooted at x; 0 if x is null
    private int size(Node x) {
        if (x == null) return 0;
        return x.size;
    }

    public int size() {
        return size(root);
    }

    public boolean isEmpty() {
        return root == null;
    }

    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("argument to get() is null");
        return get(root, key);
    }

    // value associated with the given key in subtree rooted at x; null if no such key
    private Value get(Node x, Key key) {
        while (x != null) {
            int cmp = key.compareTo(x.key);
            if      (cmp < 0) x = x.left;
            else if (cmp > 0) x = x.right;
            else              return x.val;
        }
        return null;
    }

    public boolean contains(Key key) {
        return get(key) != null;
    }

    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("first argument to put() is null");
        if (val == null) {
            delete(key);
            return;
        }

        root = put(root, key, val);
        root.color = BLACK;
        // assert check();
    }

    private Node put(Node h, Key key, Value val) {
        Node newNode = new Node(key, val, RED, 1);
        if (h == null)
            return newNode;

        // in the first while loop, we traverse down the tree
        // finding the appropriate place for the node and while
        // traversing, we store node paths
        Stack<Node> stack = new Stack<>();
        Node current = null;
        Node root = h;// primary root of the tree; for the return statement if
                      // the key intended for insertion already exists

        // for this loop our base case is to reach down the tree, i.e. node is null
        while (h != null) {
            // current node will be pointing to the last node that we will encounter before
            // reaching the end i.e. this will be the node to which we will attach new node
            current = h;
            int cmp = key.compareTo(h.key);
            // if key is greater than current node, save node and go to the left
            if (cmp < 0) {
                stack.push(h);
                h = h.left;
            } else if (cmp > 0) {
                // if key is greater than current node, save node and go to the right
                stack.push(h);
                h = h.right;
            } else {
                h.val = val;
                return root;
            }
        }

        // base case achieved; last comparison for insertion
        int cmp = key.compareTo(current.key);
        if (cmp < 0)
            current.left = newNode;// if key is greater than current node, ATTACH new node to the left
        else
            current.right = newNode;// if key is less than current node, ATTACH new node to the right

        // we now start traversing back to the root
        // we pop and check if any rb violations occur, and resolve them.
        // and then attach the updated node to its parent node by the peek technique.
        // we do this, unless we reach the end of stack or root
        Node pop = null;
        while (!stack.isEmpty()) {
            // popping the next parent node from the stack.
            pop = stack.pop();
            // fix any right-leaning links
            // if this node is not the last element in stack/or root
            // check its parent. and attach this updated node after rb tree violation gets fixed
            // in the appropriate direction i.e. left or right child is updated after resolving
            // any rb-tree violation.
            if (isRed(pop.right) && !isRed(pop.left)) {
                // rotating left, resolving rb violation, fixing the links
                pop = rotateLeft(pop);
                // checking if stack is still non empty, meaning this is not the last node or parent node.
                // we need to update the current node's parent with MODIFIED node.
                if (!stack.isEmpty()) {
                    // here we peek, without popping from the stack
                    Node peek = stack.peek();
                    // ATTACH--MODIFIED left or right node. peek is PARENT and pop is CHILD
                    if (pop.key.compareTo(peek.key) < 0) {
                        peek.left = pop;
                    } else {
                        peek.right = pop;
                    }
                }
            }
            if (isRed(pop.left) && isRed(pop.left.left)) {
                // rotating node right, resolving rb violation.
                pop = rotateRight(pop);
                // checking if stack is still non empty, meaning this is not the last node or parent node
                // as we need to update the current node parent with modified node details.
                if (!stack.isEmpty()) {
                    // here we peek
                    Node peek = stack.peek();
                    // ATTACH--MODIFIED left or right node.
                    if (pop.key.compareTo(peek.key) < 0) {
                        peek.left = pop;
                    } else {
                        peek.right = pop;
                    }
                }
            }
            if (isRed(pop.left) && isRed(pop.right))
                flipColors(pop);
        }
        return pop;// pop is the analogue to h in:
                   // https://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/RedBlackBST.java.html
    }

    public void deleteMin() {
        if (isEmpty()) throw new NoSuchElementException("BST underflow");

        // if both children of root are black, set root to red
        if (!isRed(root.left) && !isRed(root.right))
            root.color = RED;

        root = deleteMin(root);
        if (!isEmpty()) root.color = BLACK;
        // assert check();
    }

    // delete the key-value pair with the minimum key rooted at h
    private Node deleteMin(Node h) {
        if (h.left == null)
            return null;

        if (!isRed(h.left) && !isRed(h.left.left))
            h = moveRedLeft(h);

        h.left = deleteMin(h.left);
        return balance(h);
    }

    public void deleteMax() {
        if (isEmpty()) throw new NoSuchElementException("BST underflow");

        // if both children of root are black, set root to red
        if (!isRed(root.left) && !isRed(root.right))
            root.color = RED;

        root = deleteMax(root);
        if (!isEmpty()) root.color = BLACK;
        // assert check();
    }

    // delete the key-value pair with the maximum key rooted at h
    private Node deleteMax(Node h) {
        if (isRed(h.left))
            h = rotateRight(h);

        if (h.right == null)
            return null;

        if (!isRed(h.right) && !isRed(h.right.left))
            h = moveRedRight(h);

        h.right = deleteMax(h.right);

        return balance(h);
    }

    private void fixupRoot() {
        // if both children of root are black, set root to red
        if (!isRed(root.left) && !isRed(root.right)) {
            root.color = RED;
        }
    }


    public void delete(Key key) {
        if (key == null)
            throw new IllegalArgumentException("argument to delete() is null");
        if (!contains(key))
            return;

        fixupRoot();

        root = delete(root, key);
        if (!isEmpty())
            root.color = BLACK;
        assert check();
    }
    // solution from introduction to algorithms 3rd edition by cormen

    // we use 2 stacks here. nodeStack is used to store the parent node and isLeftDir is used to store the direction
    // i.e. iterate and traverse h to the right or left while pushing nodes onto the stack up to the node of deletion
    // as we traverse down the tree, we also update the balance properties
    // note that there are two delete cases: one with null stack push/break and one with a call to deleteMin()/break
    // in the first while loop, we keep a trace of where we've been with two stacks and begin to explore that
    // trace in the second while loop after finding the delete key
    // then we traverse back up to the root through stack manipulation, reattach and re-balance parent nodes as we go

    private Node delete(Node h, Key key) {
        assert get(h, key) != null;
        // to store parent nodes or path
        Stack<Node> nodeStack = new Stack<>();
        // to store direction of nodes. if direction is left we store true, else false
        Stack<Boolean> isLeftDir = new Stack<>();
        // starting from root, until the end of the tree
        while (h != null) {
            // check if the key to delete is less than current node, go left
            if (key.compareTo(h.key) < 0) {
                if (!isRed(h.left) && !isRed(h.left.left))
                    h = moveRedLeft(h);
                nodeStack.push(h); // store node in stack
                isLeftDir.push(true); // store left direction
                h = h.left;
            } else {
                // check if the key to delete is greater than the current node, go right
                if (isRed(h.left))
                    h = rotateRight(h);
                if (key.compareTo(h.key) == 0 && (h.right == null)) {
                    // this means we have reached the node, so push null onto
                    // the stack since it needs to be deleted

                    // no need to store direction
                    nodeStack.push(null);
                    break;
                }
                if (!isRed(h.right) && !isRed(h.right.left))
                    h = moveRedRight(h);
                if (key.compareTo(h.key) == 0) {
                    Node x = min(h.right);
                    h.key = x.key;
                    h.val = x.val;
                    // h.val = get(h.right, min(h.right).key);
                    // h.key = min(h.right).key;

                    // this means we have reached the node, and the node to delete is
                    // on the right after deletion, update the node and push it on the stack
                    // since this is the deletion node, no need to store direction
                    h.right = deleteMin(h.right);
                    nodeStack.push(h);
                    break;
                } else {
                    nodeStack.push(h);// store node in stack.
                    isLeftDir.push(false);// store right direction.
                    h = h.right;
                }
            }
        }

        // we now start traversing back to the root
        // we pop node elements and directions
        // and update parent nodes with modified nodes and perform re-balancing
        // we do this, until we reach the end of the stack or root
        Node node = null;
        while (!nodeStack.isEmpty()) {
            // pop the next node from the stack
            node = nodeStack.pop();
            // if the stack is non empty, this means the node has a parent.
            // so update the parent with the modified node
            if (!nodeStack.isEmpty()) {
                // get parent of current node
                Node parent = nodeStack.pop();
                // if direction stored against current node was left, attach it to the left
                if (isLeftDir.pop()) {
                    parent.left = node;
                } else {
                    parent.right = node;// attach it to right
                }
                // balance the parent and push it back to the stack for the next iteration
                nodeStack.push(balance(parent));
            } else {
                // if it was the last node i.e. parent/root perform balancing
                balance(node);
            }
        }
        return node;
    }


    // make a left-leaning link lean to the right
    private Node rotateRight(Node h) {
        // assert (h != null) && isRed(h.left);
        Node x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = x.right.color;
        x.right.color = RED;
        x.size = h.size;
        h.size = size(h.left) + size(h.right) + 1;
        return x;
    }

    // make a right-leaning link lean to the left
    private Node rotateLeft(Node h) {
        // assert (h != null) && isRed(h.right);
        Node x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = x.left.color;
        x.left.color = RED;
        x.size = h.size;
        h.size = size(h.left) + size(h.right) + 1;
        return x;
    }

    // flip the colors of a node and its two children
    private void flipColors(Node h) {
        // h must have opposite color of its two children
        // assert (h != null) && (h.left != null) && (h.right != null);
        // assert (!isRed(h) &&  isRed(h.left) &&  isRed(h.right))
        //    || (isRed(h)  && !isRed(h.left) && !isRed(h.right));
        h.color = !h.color;
        h.left.color = !h.left.color;
        h.right.color = !h.right.color;
    }

    // Assuming that h is red and both h.left and h.left.left
    // are black, make h.left or one of its children red.
    private Node moveRedLeft(Node h) {
        // assert (h != null);
        // assert isRed(h) && !isRed(h.left) && !isRed(h.left.left);

        flipColors(h);
        if (isRed(h.right.left)) {
            h.right = rotateRight(h.right);
            h = rotateLeft(h);
            flipColors(h);
        }
        return h;
    }

    // Assuming that h is red and both h.right and h.right.left
    // are black, make h.right or one of its children red.
    private Node moveRedRight(Node h) {
        // assert (h != null);
        // assert isRed(h) && !isRed(h.right) && !isRed(h.right.left);
        flipColors(h);
        if (isRed(h.left.left)) {
            h = rotateRight(h);
            flipColors(h);
        }
        return h;
    }

    public int height() {
        return height(root);
    }
    private int height(Node x) {
        if (x == null) return -1;
        return 1 + Math.max(height(x.left), height(x.right));
    }

    public Key min() {
        if (isEmpty()) throw new NoSuchElementException("calls min() with empty symbol table");
        return min(root).key;
    }

    // the smallest key in subtree rooted at x; null if no such key
    private Node min(Node x) {
        // assert x != null;
        if (x.left == null) return x;
        else                return min(x.left);
    }

    public Key max() {
        if (isEmpty()) throw new NoSuchElementException("calls max() with empty symbol table");
        return max(root).key;
    }

    // the largest key in the subtree rooted at x; null if no such key
    private Node max(Node x) {
        // assert x != null;
        if (x.right == null) return x;
        else                 return max(x.right);
    }

    public Key floor(Key key) {
        if (key == null) throw new IllegalArgumentException("argument to floor() is null");
        if (isEmpty()) throw new NoSuchElementException("calls floor() with empty symbol table");
        Node x = floor(root, key);
        if (x == null) throw new NoSuchElementException("argument to floor() is too small");
        else           return x.key;
    }

    // the largest key in the subtree rooted at x less than or equal to the given key
    private Node floor(Node x, Key key) {
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if (cmp == 0) return x;
        if (cmp < 0)  return floor(x.left, key);
        Node t = floor(x.right, key);
        if (t != null) return t;
        else           return x;
    }

    public Key ceiling(Key key) {
        if (key == null) throw new IllegalArgumentException("argument to ceiling() is null");
        if (isEmpty()) throw new NoSuchElementException("calls ceiling() with empty symbol table");
        Node x = ceiling(root, key);
        if (x == null) throw new NoSuchElementException("argument to ceiling() is too small");
        else           return x.key;
    }

    // the smallest key in the subtree rooted at x greater than or equal to the given key
    private Node ceiling(Node x, Key key) {
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if (cmp == 0) return x;
        if (cmp > 0)  return ceiling(x.right, key);
        Node t = ceiling(x.left, key);
        if (t != null) return t;
        else           return x;
    }

    public Key select(int rank) {
        if (rank < 0 || rank >= size()) {
            throw new IllegalArgumentException("argument to select() is invalid: " + rank);
        }
        return select(root, rank);
    }

    // Return key in BST rooted at x of given rank.
    // Precondition: rank is in legal range.
    private Key select(Node x, int rank) {
        if (x == null) return null;
        int leftSize = size(x.left);
        if      (leftSize > rank) return select(x.left,  rank);
        else if (leftSize < rank) return select(x.right, rank - leftSize - 1);
        else                      return x.key;
    }

    public int rank(Key key) {
        if (key == null) throw new IllegalArgumentException("argument to rank() is null");
        return rank(key, root);
    }

    // number of keys less than key in the subtree rooted at x
    private int rank(Key key, Node x) {
        if (x == null) return 0;
        int cmp = key.compareTo(x.key);
        if      (cmp < 0) return rank(key, x.left);
        else if (cmp > 0) return 1 + size(x.left) + rank(key, x.right);
        else              return size(x.left);
    }

    public Iterable<Key> keys() {
        if (isEmpty()) return new Queue<Key>();
        return keys(min(), max());
    }

    public Iterable<Key> keys(Key lo, Key hi) {
        if (lo == null) throw new IllegalArgumentException("first argument to keys() is null");
        if (hi == null) throw new IllegalArgumentException("second argument to keys() is null");

        Queue<Key> queue = new Queue<Key>();
        // if (isEmpty() || lo.compareTo(hi) > 0) return queue;
        keys(root, queue, lo, hi);
        return queue;
    }

    // add the keys between lo and hi in the subtree rooted at x
    // to the queue
    private void keys(Node x, Queue<Key> queue, Key lo, Key hi) {
        if (x == null) return;
        int cmplo = lo.compareTo(x.key);
        int cmphi = hi.compareTo(x.key);
        if (cmplo < 0) keys(x.left, queue, lo, hi);
        if (cmplo <= 0 && cmphi >= 0) queue.enqueue(x.key);
        if (cmphi > 0) keys(x.right, queue, lo, hi);
    }

    public int size(Key lo, Key hi) {
        if (lo == null) throw new IllegalArgumentException("first argument to size() is null");
        if (hi == null) throw new IllegalArgumentException("second argument to size() is null");

        if (lo.compareTo(hi) > 0) return 0;
        if (contains(hi)) return rank(hi) - rank(lo) + 1;
        else              return rank(hi) - rank(lo);
    }

    private boolean check() {
        if (!isBST())            StdOut.println("Not in symmetric order");
        if (!isSizeConsistent()) StdOut.println("Subtree counts not consistent");
        if (!isRankConsistent()) StdOut.println("Ranks not consistent");
        if (!is23())             StdOut.println("Not a 2-3 tree");
        if (!isBalanced())       StdOut.println("Not balanced");
        return isBST() && isSizeConsistent() && isRankConsistent() && is23() && isBalanced();
    }

    // does this binary tree satisfy symmetric order?
    // Note: this test also ensures that data structure is a binary tree since order is strict
    private boolean isBST() {
        return isBST(root, null, null);
    }

    // is the tree rooted at x a BST with all keys strictly between min and max
    // (if min or max is null, treat as empty constraint)
    // Credit: Bob Dondero's elegant solution
    private boolean isBST(Node x, Key min, Key max) {
        if (x == null) return true;
        if (min != null && x.key.compareTo(min) <= 0) return false;
        if (max != null && x.key.compareTo(max) >= 0) return false;
        return isBST(x.left, min, x.key) && isBST(x.right, x.key, max);
    }

    // are the size fields correct?
    private boolean isSizeConsistent() { return isSizeConsistent(root); }
    private boolean isSizeConsistent(Node x) {
        if (x == null) return true;
        if (x.size != size(x.left) + size(x.right) + 1) return false;
        return isSizeConsistent(x.left) && isSizeConsistent(x.right);
    }

    // check that ranks are consistent
    private boolean isRankConsistent() {
        for (int i = 0; i < size(); i++)
            if (i != rank(select(i))) return false;
        for (Key key : keys())
            if (key.compareTo(select(rank(key))) != 0) return false;
        return true;
    }

    // Does the tree have no red right links, and at most one (left)
    // red links in a row on any path?
    private boolean is23() { return is23(root); }
    private boolean is23(Node x) {
        if (x == null) return true;
        if (isRed(x.right)) return false;
        if (x != root && isRed(x) && isRed(x.left))
            return false;
        return is23(x.left) && is23(x.right);
    }

    // do all paths from root to leaf have same number of black edges?
    private boolean isBalanced() {
        int black = 0;     // number of black links on path from root to min
        Node x = root;
        while (x != null) {
            if (!isRed(x)) black++;
            x = x.left;
        }
        return isBalanced(root, black);
    }

    // does every path from the root to a leaf have the given number of black links?
    private boolean isBalanced(Node x, int black) {
        if (x == null) return black == 0;
        if (!isRed(x)) black--;
        return isBalanced(x.left, black) && isBalanced(x.right, black);
    }

    // restore red-black tree invariant
    private Node balance(Node h) {
        // assert (h != null);

        if (isRed(h.right))                      h = rotateLeft(h);
        if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left) && isRed(h.right))     flipColors(h);
        h.size = size(h.left) + size(h.right) + 1;
        return h;
    }

    public static void main(String[] args) {
        RedBlackBST<String, Integer> st = new RedBlackBST<>();
        for (int i = 0; !StdIn.isEmpty(); i++) {
            String key = StdIn.readString();
            st.put(key, i);
        }
        StdOut.println();
        for (String s : st.keys())
            StdOut.println(s + " " + st.get(s));
        StdOut.println();
        //StdOut.println(st.check());
        st.delete("t");
        System.out.println(st.keys());
    }
}
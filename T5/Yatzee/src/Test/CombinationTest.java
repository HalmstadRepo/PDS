package Test;

import Game.Combinations.*;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class CombinationTest {

    @Test
    public void ones() {
        int[] v = new int[]{1, 1, 1, 1, 5};
        Ones test = new Ones();
        int score = test.calculateScore(v);
        Assert.assertEquals(4, score);
    }
    @Test
    public void threeOfAKind() {
        int[] v = new int[]{1, 1, 1, 1, 5};
        ThreeOfAKind test = new ThreeOfAKind();
        int score = test.calculateScore(v);
        Assert.assertEquals(3, score);

        v = new int[]{5, 5, 5, 5, 5};
        score = test.calculateScore(v);
        Assert.assertEquals(15, score);
    }
    @Test
    public void fourOfAKind() {
        int[] v = new int[]{1, 1, 1, 1, 5};
        FourOfAKind test = new FourOfAKind();
        int score = test.calculateScore(v);
        Assert.assertEquals(4, score);

        v = new int[]{5, 5, 5, 5, 5};
        score = test.calculateScore(v);
        Assert.assertEquals(20, score);
    }
    @Test
    public void smallStraight() {
        int[] v = new int[]{2, 3, 4, 5, 6};
        SmallStraight test = new SmallStraight();
        int score = test.calculateScore(v);
        Assert.assertEquals(30, score);

        v = new int[]{1, 2, 3, 4, 6};
        score = test.calculateScore(v);
        Assert.assertEquals(30, score);

        v = new int[]{1, 2, 3, 6, 6};
        score = test.calculateScore(v);
        Assert.assertEquals(0, score);
    }

    @Test
    public void largeStraight() {
        int[] v = new int[]{2, 3, 4, 5, 6};
        LargeStraight test = new LargeStraight();
        int score = test.calculateScore(v);
        Assert.assertEquals(40, score);

        v = new int[]{1, 2, 3, 4, 6};
        score = test.calculateScore(v);
        Assert.assertEquals(0, score);
    }

    @Test
    public void fullHouse() {
        int[] v = new int[]{2, 2, 2, 3, 3};
        FullHouse test = new FullHouse();
        int score = test.calculateScore(v);
        Assert.assertEquals(25, score);

        v = new int[]{1, 2, 3, 4, 6};
        score = test.calculateScore(v);
        Assert.assertEquals(0, score);

        v = new int[]{4, 1, 4, 1, 3};
        score = test.calculateScore(v);
        Assert.assertEquals(0, score);
    }
    @Test
    public void yahtzee() {
        int[] v = new int[]{2, 2, 2, 2, 3};
        Yahtzee test = new Yahtzee();
        int score = test.calculateScore(v);
        Assert.assertEquals(0, score);

        v = new int[]{2, 2, 2, 2, 2};
        score = test.calculateScore(v);
        Assert.assertEquals(50, score);

        v = new int[]{5, 5, 5, 5, 5};
        score = test.calculateScore(v);
        Assert.assertEquals(50, score);
    }
}
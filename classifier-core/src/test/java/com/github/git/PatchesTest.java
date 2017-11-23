package com.github.git;


import github.git.PatchStatistics;
import github.git.Patches;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class PatchesTest {

    @Test
    public void testModifiedLines() throws Exception{

        String patch = Patches.generatePatch("hello", "hella", "text.txt");
        PatchStatistics ps = Patches.getPatchStatistics(patch);

        Assert.assertEquals(1, ps.getModifications());
        Assert.assertEquals(0, ps.getAdditions());
        Assert.assertEquals(0, ps.getDeletions());
    }

    @Test
    public void testAddedLines() throws Exception{

        String patch = Patches.generatePatch("hello\n", "hello\nbye", "text.txt");
        PatchStatistics ps = Patches.getPatchStatistics(patch);

        Assert.assertEquals(0, ps.getModifications());
        Assert.assertEquals(1, ps.getAdditions());
        Assert.assertEquals(0, ps.getDeletions());
    }

    @Test
    public void testRemovedLines() throws Exception{

        String patch = Patches.generatePatch("hello\nbye", "hello\n", "text.txt");
        PatchStatistics ps = Patches.getPatchStatistics(patch);

        Assert.assertEquals(0, ps.getModifications());
        Assert.assertEquals(0, ps.getAdditions());
        Assert.assertEquals(1, ps.getDeletions());
    }

    @Test
    public void testRealPatch() throws Exception{

        String patch = FileUtils.readFileToString(new File("src/test/resources/sample.patch"));
        PatchStatistics ps = Patches.getPatchStatistics(patch);

        Assert.assertEquals(66, ps.getModifications());
        Assert.assertEquals(22, ps.getAdditions());
        Assert.assertEquals(0, ps.getDeletions());
    }
}

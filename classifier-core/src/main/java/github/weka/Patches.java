package github.weka;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Patches {

    public static String generatePatch(String originalText, String text, String location) {
        List<String> original = Arrays.asList(originalText.split("\n"));
        List<String> revised = Arrays.asList(text.split("\n"));

        Patch<String> patches = DiffUtils.diff(original, revised);
        List<String> unifiedDiffs = DiffUtils.generateUnifiedDiff("a" + File.separator + location,
                "b" + File.separator + location, original, patches, 4);
        Iterator<String> it = unifiedDiffs.iterator();

        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public static String applyPatch(String text, String patch) throws PatchFailedException {
        List<String> original = Arrays.asList(text.split("\n"));
        List<String> diff = Arrays.asList(patch.split("\n"));

        Patch<String> generatedPatch = DiffUtils.parseUnifiedDiff(diff);
        List<Delta<String>> deltas = generatedPatch.getDeltas();


        Iterator<String> it = DiffUtils.patch(original, generatedPatch).iterator();
        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public static PatchStatistics getPatchStatistics(String patch) {

        List<String> diff = Arrays.asList(patch.split("\n"));

        Patch<String> generatedPatch = DiffUtils.parseUnifiedDiff(diff);
        List<Delta<String>> deltas = generatedPatch.getDeltas();

        Iterator<Delta<String>> it = deltas.iterator();

        int modifications = 0;
        int additions = 0;
        int deletions = 0;

        while (it.hasNext()) {

            Delta<String> delta = it.next();

            Chunk<String> original = delta.getOriginal();
            Chunk<String> revised = delta.getRevised();

            int originalSize = original.size();
            int revisedSize = revised.size();
            int difference = Math.abs(revisedSize - originalSize);

            if (revisedSize > originalSize) {
                additions += difference;
                modifications += originalSize - 1 ;
            } else if (revisedSize < originalSize) {
                deletions += difference;
                modifications += revisedSize - 1;
            } else {
                modifications += originalSize;
            }
        }

        return new PatchStatistics(additions, modifications, deletions);
    }


}

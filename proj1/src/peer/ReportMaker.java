package peer;

import files.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * This class represents a useless class for the project grade at least, but we has some fun
 * while designing it, this class builds a Report in a HTML form, using bootstrap and basic HTML.
 */
public class ReportMaker {
    private final static String header = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-BmbxuPwQa2lc/FVzBcNJ7UAyJxM6wuqIj61tLrc4wSX0szH/Ev+nYRRuWlolflfl\" crossorigin=\"anonymous\">\n" +
            "    <script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/js/bootstrap.bundle.min.js\" integrity=\"sha384-b5kHyXgcpbZJO/tY9Ul7kGkf1S0CWuKcCD38l8YkeH8z8QjE0GmW1gYU5S9FOnJ0\" crossorigin=\"anonymous\" defer></script>\n" +
            "    <title>State</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<nav class=\"navbar navbar-dark bg-dark\">\n" +
            "    <div class=\"container-fluid\">\n" +
            "        <span class=\"navbar-brand mb-0 h1\">Distributed Systems</span>\n" +
            "    </div>\n" +
            "</nav>";
    private final static String footer = "</body>\n" +
            "</html>";

    private final static String title = "<h1 class=\"m-3\">Peer %d State Report</h1>";

    private final static String storage = "<div class=\"p-3 d-flex flex-column\">\n" +
            "        <span class=\"h4\">Capacity: %.2fKB</span>\n" +
            "        <span class=\"h4\">Occupation: %.2fKB</span>\n" +
            "    </div>";

    private final static String backedFile = "<li class=\"list-group-item active d-flex\" aria-current=\"true\">\n" +
            "                <div class=\"col-2\">Pathname: %s</div>\n" +
            "                <div class=\"col-5\">FileID: %s</div>\n" +
            "                <div class=\"col-2\">Replication Degree: %d</div>\n" +
            "                <div class=\"col-3\">Size: %.2fKB</div>\n" +
            "            </li>";

    private final static String sentChunk = "<li class=\"list-group-item d-flex\">\n" +
            "                <div class=\"col-3\">ChunkNo: %d</div>\n" +
            "                <div class=\"col-3\">Actual Replication Degree: %d</div>\n" +
            "            </li>";

    private final static String savedChunk = "<li class=\"list-group-item d-flex\">\n" +
            "                <div class=\"col-4\">FileID: %s</div>\n" +
            "                <div class=\"col-2\">ChunkNo: %d</div>\n" +
            "                <div class=\"col-2\">Desired Replication Degree: %d</div>\n" +
            "                <div class=\"col-2\">Perceived Replication Degree: %d</div>\n" +
            "                <div class=\"col-2\">Size: %.2fKB</div>\n" +
            "            </li>";

    /**
     * Method to convert the PeerInternalState to an HTML file, the web page will open after the creation is done,
     * and will be stored with a hashed filename using SHA256 as we have already developed that method in IOUtils
     *
     * @param state The Peer's Internal State to use here
     * @see PeerInternalState
     * @see IOUtils#hashToASCII(String)
     * @see Peer#state()
     */
    public static void toHTML(PeerInternalState state) {
        StringBuilder builder = new StringBuilder();
        builder.append(header);
        builder.append(String.format(title, state.peer.getPeerId()));
        builder.append(String.format(storage, state.getCapacity() / 1000.0, state.getOccupation() / 1000.0));
        builder.append(" <div class=\"p-3\">\n" +
                "        <h2>Backed Up Files</h2>\n" +
                "        <ul class=\"list-group mt-3\">");
        for (Map.Entry<String, ServerFile> serverFileEntry : state.getBackedUpFilesMap().entrySet()) {
            ServerFile file = serverFileEntry.getValue();

            builder.append(String.format(backedFile, serverFileEntry.getKey(), file.getFileId(), file.getReplicationDegree(), file.getSize()));

            List<SentChunk> chunks = new ArrayList<>();

            for (Map.Entry<String, SentChunk> sentChunkEntry : state.getSentChunksMap().entrySet()) {
                SentChunk chunk = sentChunkEntry.getValue();
                if (chunk.getFileId().equals(file.getFileId()))
                    chunks.add(chunk);
            }
            chunks.sort(Comparator.comparingInt(Chunk::getChunkNo));
            for (SentChunk chunk : chunks) {
                builder.append(String.format(sentChunk, chunk.getChunkNo(), chunk.getPeers().size()));
            }
        }
        builder.append("</ul>\n" +
                "        <hr/>\n" +
                "        <h2>Saved Chunks</h2>\n" +
                "        <ul class=\"list-group mt-3\">");

        for (Map.Entry<String, SavedChunk> savedChunkEntry : state.getSavedChunksMap().entrySet()) {
            SavedChunk chunk = savedChunkEntry.getValue();
            builder.append(String.format(savedChunk, chunk.getFileId(), chunk.getChunkNo(), chunk.getReplicationDegree(), chunk.getPeers().size(), chunk.getSize()));
        }

        builder.append("</ul>\n" +
                "    </div>");
        builder.append(footer);


        String html = builder.toString();

        String sha = IOUtils.hashToASCII(html);

        File report = new File(sha + ".html");
        try {
            Files.write(report.toPath(), html.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

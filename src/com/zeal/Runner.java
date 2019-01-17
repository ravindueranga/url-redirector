package com.zeal;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

class Runner {

    void run() throws IOException {

        final String folder = "files";
        final String redirectedList = "redirected lists";

        boolean isCreated = createFolder(redirectedList);

        List<String> fileList = listFilesForFolder(folder);

        for (String s : fileList) {

            System.out.println(s);

            FileWriter fw = new FileWriter(redirectedList + "/" + s.substring(0, s.length() - 4) + "_redir.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);

            try {
                BufferedReader br = new BufferedReader(new FileReader(folder + "/" + s));
                String str;
                while ((str = br.readLine()) != null) {

                    try {
                        str = str.replace("\uFEFF", "");

                        HttpURLConnection con = createConnection(str);
                        con.connect();

                        if (con.getResponseCode() != 200) {

                            String location = con.getHeaderField("Location");
                            if (location == null || !location.contains("http")) {
                                out.println(str);
                            } else {
                                out.println(location);
                            }

                        } else {
                            out.println(str);
                        }
                    } catch (Exception e) {
                        out.println(str);
                    }

                }

            } finally {
                out.close();
                System.out.println("Finished : " + s);
            }
        }

        System.out.println("Done getting redirected URLs");
    }

    private HttpURLConnection createConnection(String str) {

        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) (new URL(str).openConnection());
        } catch (IOException e) {
            e.printStackTrace();
        }
        con.setConnectTimeout(2000);
        con.setReadTimeout(2000);
        con.setInstanceFollowRedirects(false);

        return con;
    }

    private boolean createFolder(String redirectedList) {

        File f = new File(redirectedList);
        boolean isCreated = f.mkdir();

        if (isCreated) {
            System.out.println("Folder Created");
        }

        return isCreated;
    }

    /**
     * Get all the file names in a given folder
     *
     * @param folderPath
     * @return
     * @throws IOException
     */
    private List<String> listFilesForFolder(final String folderPath) throws IOException {

        return Files.walk(Paths.get(folderPath)).filter(Files::isRegularFile).map(Path::toFile).map(File::getName).collect(Collectors.toList());
    }
}

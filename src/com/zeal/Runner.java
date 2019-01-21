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

    static final String FOLDER_NAME = "files";
    static final String REDIRECTED_LIST = "redirected lists";
    static final int RECORD_THRESHOLD = 500;

    void run() throws IOException {

        createFolder(REDIRECTED_LIST);

        List<String> fileList = listFilesForFolder(FOLDER_NAME);

        for (String s : fileList) {

            int counter = 0;

            System.out.println(s);

            FileWriter fw = new FileWriter(REDIRECTED_LIST + "/" + s.substring(0, s.length() - 4) + "_redir.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);

            try {
                BufferedReader br = new BufferedReader(new FileReader(FOLDER_NAME + "/" + s));
                String str;
                while ((str = br.readLine()) != null) {
                    ++counter;
                    if (counter % RECORD_THRESHOLD == 0) {
                        System.out.println(RECORD_THRESHOLD + " urls reached");
                    }
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
     * Get all the file names in a given FOLDER_NAME
     *
     * @param folderPath
     * @return
     * @throws IOException
     */
    private List<String> listFilesForFolder(final String folderPath) throws IOException {

        return Files.walk(Paths.get(folderPath)).filter(Files::isRegularFile).map(Path::toFile).map(File::getName).collect(Collectors.toList());
    }
}

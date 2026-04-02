//Domenica Herrera  ID:2427467

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

class Movie implements Comparable<Movie> {
    private final String title;
    private final int year;
    private final String genre;
    private final double avgRating;

    public Movie(String title, int year, String genre, int rating1, int rating2, int rating3) {
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.avgRating = (rating1 + rating2 + rating3) / 3.0;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public String getGenre() {
        return genre;
    }

    public double getAverageRating() {
        return avgRating;
    }

    @Override
    public int compareTo(Movie other) {
        return Double.compare(other.avgRating, this.avgRating); // descending
    }
}

public class MovieAnalyzer {

    private static final String[] REQUIRED_HEADERS = {
        "title", "year", "genre", "rating1", "rating2", "rating3"
    };

    private static String cleanTextField(String s) {
        return s.replace('\t', ' ').trim();
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static boolean validHeader(String line) {
        if (line == null) {
            System.err.println("Error: file is empty or missing a header.");
            return false;
        }

        line = line.replace("\uFEFF", ""); 

        String[] parts = line.split(",", -1);
        if (parts.length != 6) {
            System.err.println("Error: header must contain exactly 6 columns.");
            return false;
        }

        for (int i = 0; i < 6; i++) {
            String actual = parts[i].trim().toLowerCase();
            if (actual.isEmpty()) {
                System.err.println("Error: header contains a blank column name.");
                return false;
            }
            if (!actual.equals(REQUIRED_HEADERS[i])) {
                System.err.println("Error: invalid header. Expected:");
                System.err.println("Title,Year,Genre,Rating1,Rating2,Rating3");
                return false;
            }
        }

        return true;
    }

    private static String findHeader(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                return line;
            }
        }
        return null;
    }

    private static boolean isValidMovieRecord(String line, int lineNumber) {
        if (line == null || line.trim().isEmpty()) {
            return false; //blank lines are not allowed, we skipped them 
        }

        String[] parts = line.split(",", -1);

        if (parts.length != 6) {
            System.err.println("Invalid record on line " + lineNumber +
                    ": wrong number of values.");
            return false;
        }

        for (String part : parts) {
            if (part.trim().isEmpty()) {
                System.err.println("Invalid record on line " + lineNumber +
                        ": blank value found.");
                return false;
            }
        }

        String title = cleanTextField(parts[0]);
        String genre = cleanTextField(parts[2]);

        if (title.isEmpty() || genre.isEmpty()) {
            System.err.println("Invalid record on line " + lineNumber +
                    ": title or genre is blank.");
            return false;
        }
//check for valid year and ratings
        try {
            int year = Integer.parseInt(parts[1].trim());
            int rating1 = Integer.parseInt(parts[3].trim());
            int rating2 = Integer.parseInt(parts[4].trim());
            int rating3 = Integer.parseInt(parts[5].trim());

            if (year < 1901 || year > 2100) {
                System.err.println("Invalid record on line " + lineNumber +
                        ": year must be between 1901 and 2100.");
                return false;
            }

            if (rating1 < 1 || rating1 > 100 ||
                rating2 < 1 || rating2 > 100 ||
                rating3 < 1 || rating3 > 100) {
                System.err.println("Invalid record on line " + lineNumber +
                        ": ratings must be integers between 1 and 100.");
                return false;
            }

        } catch (NumberFormatException e) {
            System.err.println("Invalid record on line " + lineNumber +
                    ": year and ratings must be integers.");
            return false;
        }

        return true;
    }

    private static int countValidRecords(File file) {
        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = findHeader(br);
            if (!validHeader(header)) {
                return -1;
            }

            String line;
            int lineNumber = 1; 

            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue; 
                }

                if (isValidMovieRecord(line, lineNumber)) {
                    count++;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return -1;
        } catch (Exception e) {
            System.err.println("Unexpected error while counting records: " + e.getMessage());
            return -1;
        }

        return count;
    }

    private static Movie[] loadMovies(File file, int validCount) {
        Movie[] movies = new Movie[validCount];
        int index = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = findHeader(br);
            if (!validHeader(header)) {
                return null;
            }

            String line;
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                if (!isValidMovieRecord(line, lineNumber)) {
                    continue;
                }

                String[] parts = line.split(",", -1);

                String title = cleanTextField(parts[0]);
                int year = Integer.parseInt(parts[1].trim());
                String genre = cleanTextField(parts[2]);
                int rating1 = Integer.parseInt(parts[3].trim());
                int rating2 = Integer.parseInt(parts[4].trim());
                int rating3 = Integer.parseInt(parts[5].trim());

                movies[index] = new Movie(title, year, genre, rating1, rating2, rating3);
                index++;
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error while loading movies: " + e.getMessage());
            return null;
        }

        return movies;
    }

    private static void printMovies(Movie[] movies) {
        if (movies == null || movies.length == 0) {
            return;
        }

        int titleWidth = "Title".length();
        for (Movie m : movies) {
            if (m.getTitle().length() > titleWidth) {
                titleWidth = m.getTitle().length();
            }
        }

        int genreWidth = Math.max("Genre".length(), 10);

        String headerFormat = "%-" + titleWidth + "s | %4s | %-" + genreWidth + "s | %10s%n";
        String rowFormat = "%-" + titleWidth + "s | %4d | %-" + genreWidth + "s | %10.1f%n";

        System.out.printf(headerFormat, "Title", "Year", "Genre", "Avg Rating");

        int totalWidth = titleWidth + 3 + 4 + 3 + genreWidth + 3 + 10;
        for (int i = 0; i < totalWidth; i++) {
            System.out.print("-");
        }
        System.out.println();

        for (Movie m : movies) {
            System.out.printf(rowFormat,
                    m.getTitle(),
                    m.getYear(),
                    m.getGenre(),
                    m.getAverageRating());
        }
    }

    public static void main(String[] args) {
        File file = new File("movies.csv");

        if (!file.exists()) {
            System.err.println("Error: movies.csv was not found.");
            return;
        }

        if (!file.isFile()) {
            System.err.println("Error: movies.csv is not a valid file.");
            return;
        }

        int validCount = countValidRecords(file);

        if (validCount == -1) {
            return;
        }

        if (validCount == 0) {
            System.err.println("Error: no valid movie records were found.");
            return;
        }

        Movie[] movies = loadMovies(file, validCount);

        if (movies == null) {
            return;
        }

        Arrays.sort(movies);
        printMovies(movies);
    }
}
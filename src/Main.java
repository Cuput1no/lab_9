import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.sin;


public class Main {

    static class ThreadCls extends Thread {

        private final double start,end;
        private final long step;
        private double result = 0.0;

        public ThreadCls(double start,double end,long step) {
            this.start = start;
            this.end = end;
            this.step = step;
        }



        public double getResult(){
            return result;
        }

        @Override
        public void run(){
            double h = (end - start) / step;
            double sum = 0.0;
            for (long i = 0; i < step; i++) {
                double x = start + (i + 0.5) * h;
                sum += function(x) * h;
            }
            result = sum;
        }


    }

    private static double function(double x) {
        return x * x + 7 * x + sin (2 * x);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        double a = 0.0;
        double b = 10.0;
        int totalSteps = 10_000_000;
        int maxThreads = 8;

        try (FileWriter writer = new FileWriter("result.csv", java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("Кількість потоків, Час с\n");

            for(int thread = 1; thread <= maxThreads; thread++){

                long startTime = System.nanoTime();

                List<ThreadCls> threads = new ArrayList<>();

                double stepRange = (b - a) / thread;
                long stepsPerThread = totalSteps/ thread;

                for(int i = 0; i < thread;i++){
                    double start = a + i * stepRange;

                    double end = start + stepRange;

                    long steps = (i == thread - 1) ? totalSteps - i * stepsPerThread : stepsPerThread;

                    ThreadCls Thread = new ThreadCls(start, end, steps);

                    threads.add(Thread);
                    Thread.start();
                }

                double totalResult = 0.0;

                for(ThreadCls thd : threads) {
                    try {
                        thd.join();
                        totalResult += thd.getResult();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                long endTime = System.nanoTime();
                double timeSec = (endTime - startTime) /  1000000000.0;

                String csvLine = String.format(Locale.US, "%d,%.2f", thread, timeSec);
                writer.write(csvLine + "\n");

                System.out.printf("Кількість потоків: %d; Час: %.2f с; Результат: %.6f\n", thread, timeSec, totalResult);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
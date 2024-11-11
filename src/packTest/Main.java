package packTest;

import packWork.*;

import java.io.*;
import java.util.Scanner;

public class Main {
    private static PipedInputStream inputPipe = new PipedInputStream();
    private static PipedOutputStream outputPipe = new PipedOutputStream();

    private static String FOLDER;

    public void set_FOLDER(String path) {
        FOLDER = path;
    }

    private File selectedImage = null;
    private Effect effect = null;
    private Object lock = new Object();

    public static void main(String args[]) {
        Main main = new Main();
        main.set_FOLDER(args[0]);
        main.doTest();
    }

    private void doTest() {
        File imagesDirectory = new File(FOLDER);
        if (imagesDirectory.exists()) {
            File images[] = imagesDirectory.listFiles();
            Scanner scanner = new Scanner(System.in);

            try {
                inputPipe.connect(outputPipe);

                Thread producer = new Thread(() -> produceImages(images, scanner));
                producer.start();

                Thread consumer = new Thread(this::consumeImages);
                consumer.start();

                try {
                    producer.join();
                    consumer.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                scanner.close();
            }
        }
    }

    private void produceImages(File[] images, Scanner scanner) {
        chooseImage(images, scanner);
        try (DataOutputStream dataOutput = new DataOutputStream(outputPipe)) {
            String imagePath = selectedImage.getAbsolutePath();
            
            // Split the image path into 4 segments
            int segmentLength = imagePath.length() / 4;
            for (int i = 0; i < 3; i++) {
                String segment = imagePath.substring(i * segmentLength, (i + 1) * segmentLength);
                dataOutput.writeUTF(segment);
                dataOutput.flush();

                // Print a message for each segment
                System.out.println("Producer: Enqueued segment " + (i + 1));

                synchronized (lock) {
                    lock.notify();
                }
               
                Thread.sleep(1000); // Simulate some processing time after putting the segment
            }
            // The last segment includes any remaining characters
            String lastSegment = imagePath.substring(3 * segmentLength);
            dataOutput.writeUTF(lastSegment);
            dataOutput.flush();

            // Print a message for the last segment
            System.out.println("Producer: Enqueued segment 4");

            synchronized (lock) {
                lock.notify();
            }

            System.out.println("Producer: Enqueued image " + selectedImage.getName());
            Thread.sleep(1000); // Simulate some processing time after putting the image
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void consumeImages() {
        try (DataInputStream dataInput = new DataInputStream(inputPipe)) {
            while (true) {
                // Read each segment of the image path from the pipe and assemble them
                StringBuilder imagePathBuilder = new StringBuilder();
                boolean endOfStream = false;

                for (int i = 0; i < 4; i++) {
                    try {
                        String segment = dataInput.readUTF();
                        imagePathBuilder.append(segment);

                        // Print a message for each received segment
                        System.out.println("Consumer: Received segment " + (i + 1));
                    } catch (EOFException e) {
                        // End of stream encountered
                        endOfStream = true;
                        break;
                    }
                }

                if (endOfStream) {
                    // No more data to read, terminate the consumer thread
                    break;
                }

                String imagePath = imagePathBuilder.toString();
                selectedImage = new File(imagePath);

                System.out.println("Consumer: Dequeued image " + selectedImage.getName());

                chooseContrastLevel();

                effect.setPhoto(selectedImage);

                EffectExecutor<Effect> effectExecutor = new EffectExecutor<>(effect);
                effectExecutor.applyEffect();

                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }



    private void chooseContrastLevel() {
        // You can modify this method as needed
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the desired contrast level (contrast must be in the form 0.0f):");
        do {
            String input = scanner.nextLine().toLowerCase();

            Float contrastLevel = null;
            try {
                contrastLevel = Float.parseFloat(input);
            } catch (NumberFormatException e) {
            }

            if (contrastLevel != null) {
                effect = new Contrast(contrastLevel);
            } else
                System.out.println("You entered an incorrect value. The number must be in the form 0.0f. Try again!");
        } while (effect == null);

        scanner.close();
    }

    private void chooseImage(File[] images, Scanner scanner) {
        System.out.println("Available images in the folder:");
        for (int i = 0; i < images.length; i++)
            System.out.println((i + 1) + ". " + images[i].getName());

        System.out.println();
        System.out.print("Enter the image index:");

        do {
            String input = scanner.nextLine().toLowerCase();

            Integer imageIndex = null;
            try {
                imageIndex = Integer.parseInt(input);
            } catch (NumberFormatException e) {
            }

            if (imageIndex != null) {
                if (imageIndex > 0 && imageIndex <= images.length)
                    selectedImage = images[imageIndex - 1];
                else
                    System.out.println(String.format("You entered an incorrect index (1 <= index <= %d). Try again!",
                            images.length));
            }
        } while (selectedImage == null);
        System.out.println();
    }
}
package dmst.mebede.group12.vrp;

public class Utility {

    public double[][] convertDistanceToTime(double[][] distanceMatrix) {
        double[][] timeMatrix;
        int numberOfRows = distanceMatrix[0].length;
        int numberOfCols = distanceMatrix[1].length;
        timeMatrix = new double[numberOfRows][numberOfCols];

        for(int row = 0; row < numberOfRows; row++) {
            for (int col = 0; col < numberOfCols; col++) {
                timeMatrix[row][col] = distanceMatrix[row][col] / Truck.speedKMH;
            }
        }

        return timeMatrix;
    }

    public void printMatrix(double[][] matrix) {
        int numberOfRows = matrix[0].length;
        System.out.print("\t");
        int i = 0;
        do {
            System.out.print(" " + i + "\t");
            i++;
        } while (i < numberOfRows);
        System.out.println();
        System.out.print("\t");
        for(i = 0; i < numberOfRows; i++) {
            if (i != numberOfRows - 1) {
                System.out.print("________");
            } else {
                System.out.print("____");
            }
        }
        System.out.println();
        for (i = 0; i < numberOfRows; i++) {
            System.out.print(i + "\t" + "|");
            for (int j = 0; j < numberOfRows; j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }
}


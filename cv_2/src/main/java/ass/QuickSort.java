package ass;

/**
 * @author Kamil Prochazka
 */
public class QuickSort<E extends Comparable<E>> {

    public void sort(E[] vetor, int inicio, int fim) {
        if (inicio < fim) {
            int pivo = partition(vetor, inicio, fim);
            sort(vetor, inicio, pivo - 1);
            sort(vetor, pivo + 1, fim);
        }
    }

    private void swap(E[] vetor, int i, int j) {
        E temp = vetor[j];
        vetor[j] = vetor[i];
        vetor[i] = temp;
    }

    private int partition(E[] vetor, int inicio, int fim) {
        int i = inicio + 1;
        int j = fim;
        E pivo = vetor[inicio];
        while (i <= j) {
            if (vetor[i].compareTo(pivo) <= 0) {
                i++;
            } else if (vetor[j].compareTo(pivo) > 0) {
                j--;
            } else {
                swap(vetor, i, j);
            }
        }
        swap(vetor, inicio, j);
        return j;
    }

}

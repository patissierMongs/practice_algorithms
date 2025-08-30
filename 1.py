import numpy as np
import shutil

def get_terminal_size():
    size = shutil.get_terminal_size()
    return size.columns, size.lines

def bubble_sort(arr):
    n = len(arr)
    for i in range(n):
        swapped = False
        for j in range(0, n - i - 1):
            if arr[j] > arr[j + 1]:
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
                swapped = True
        if not swapped:
            break
    return arr

def visualizer(arr, title):
    if len(arr) == 0:
        return

    print(f"\n{title}")
    print("=" * len(title))

    n = len(arr)
    cols, lines = get_terminal_size()
    min_val = min(arr)
    max_val = max(arr)

    for i, val in enumerate(arr):
        # largest = full bar, least = 1 bar
        # ex) least: 0.01 largest: 0.95 cols: 50
        # 0.01 = 1, 0.95 = 50
        # 1 + (val - min_val) * (cols - 1) / (max_val - min_val)
        bar_length= int(1 + (val - min_val) * (cols - 17) / (max_val - min_val))
        bar = '█' * bar_length
        print(f"{i:3d}: {bar} ({val:.6f})")
        

if __name__ == "__main__":
    arr=np.random.rand(30)
    visualizer(arr, "unsorted")
    visualizer(bubble_sort(arr), "sorted")

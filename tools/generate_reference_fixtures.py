"""Deterministic independent reference values using Python Fraction and Leibniz determinants."""
import argparse
from fractions import Fraction
from itertools import permutations
from pathlib import Path
import random

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "groupimp/src/test/resources/mathematics/reference.tsv"

def fixtures():
    rng = random.Random(1909)
    rows = ["# Python fractions.Fraction; determinants use permutation expansion, not elimination."]
    for _ in range(80):
        a = Fraction(rng.randint(-100, 100), rng.randint(1, 100))
        b = Fraction(rng.randint(1, 100), rng.randint(1, 100))
        rows.append("rational\t" + "\t".join(map(str, (a, b, a + b, a * b, a / b))))
    for n in (2, 3, 4):
        for _ in range(8):
            matrix = [[Fraction(rng.randint(-5, 5)) for _ in range(n)] for _ in range(n)]
            determinant = Fraction(0)
            for permutation in permutations(range(n)):
                inversions = sum(permutation[i] > permutation[j] for i in range(n) for j in range(i + 1, n))
                product = Fraction((-1) ** inversions)
                for i in range(n):
                    product *= matrix[i][permutation[i]]
                determinant += product
            rows.append("determinant\t" + str(n) + "\t" + ",".join(str(v) for row in matrix for v in row) + "\t" + str(determinant))
    return "\n".join(rows) + "\n"

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    expected = fixtures()
    if args.check:
        if not OUTPUT.exists() or OUTPUT.read_text(encoding="utf-8") != expected:
            raise SystemExit("Reference fixtures differ; investigate before regeneration.")
    else:
        OUTPUT.parent.mkdir(parents=True, exist_ok=True)
        OUTPUT.write_text(expected, encoding="utf-8", newline="\n")
    print("104 independent reference cases verified" if args.check else "104 reference cases generated")

if __name__ == "__main__":
    main()

fun main() {
    //Dat je niz cijelih brojeva nums. Napisati fukciju koja ce zamijeniti svaki element u nums
    // sa zbirom njegovih cifara.
    // Funkcija treba da vrati najmanji element u nums nakon svih zamjena.
    // Constraints:
    // 1 <= nums.length <= 100
    //1 <= nums[i] < 1000
    //Input: nums = [10,12,13,14]
    //Output: 1
    //Explanation:
    //nums becomes [1, 3, 4, 5] after all replacements, with minimum element 1.
    //Example 2:
    //Input: nums = [999,19,199]
    //Output: 10
    //Explanation:
    //nums becomes [27, 10, 19] after all replacements, with minimum element 10.

    val nums = arrayOf(999,19,199)
    val rez= minElement(nums)
    println("Najmanja vrijednost u vektoru koji je nastao sabiranjem cifara: $rez")
}

fun minElement(input: Array<Int>): Int {
    val min = input.map { calculateSumOfDigits(it) }
    return min.minOrNull() ?: 0
}

fun calculateSumOfDigits(value: Int): Int {
    var num = value
    var sumOfDigits = 0

    while (num > 0) {
        sumOfDigits += num % 10
        num /= 10
    }

    return sumOfDigits
}
fun main() {
    //U gradu Cifrogradu, postojala je lista brojeva nazvana nums koja je sadržavala
    // cijele brojeve od 0 do n - 1. Svaki broj je trebao da se pojavi tačno jednom na listi,
    // međutim, dva nestašna broja su se pojavila još jednom, čineći listu dužom nego obično.
    //
    //Kao detektiv grada, vaš zadatak je da pronađete ova dva nestašna broja.
    //  Kao reyultat funkcije Vratite niz duzine dva koji sadrži ova dva broja nestasna
    //  (u bilo kom redoslijedu), kako bi mir mogao da se vrati u Cifrograd.
    // Constraints:
    // 1 <= nums.length <= 100
    //1 <= nums[i] < 1000
    //Input: nums = [0,1,1,0]
    //
    //Output: [0,1]
    //
    //Input: nums = [0,3,2,1,3,2]
    //
    //Output: [2,3]
    //
    //Explanation:
    val nums = intArrayOf(0,1,1,0)
    val nums1 = intArrayOf(0,3,2,1,3,2)
    println(getSneakyNumbers(nums).joinToString())
    println(getSneakyNumbers(nums1).joinToString())
}

fun getSneakyNumbers(nums: IntArray): IntArray {
    val seen = mutableListOf<Int>()
    val duplicates = mutableListOf<Int>()

    for (num in nums) {
        if (num in seen) {
            duplicates.add(num)
        } else {
            seen.add(num)
        }
    }

    return duplicates.toIntArray()
}

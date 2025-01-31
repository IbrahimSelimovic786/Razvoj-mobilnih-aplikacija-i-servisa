interface Person {
    fun getAge_(): Int
    fun getCountry_(): String
}

open class Developer(
    val firstName: String,
    val lastName: String,
    val age: Int,
    val country: String,
    val programmingLanguages: List<String>
) : Person {
    override fun getAge_(): Int = age
    override fun getCountry_(): String = country
}

class BackendDeveloper(
    firstName: String,
    lastName: String,
    age: Int,
    country: String,
    programmingLanguages: List<String>,
    val backendFramework: String
) : Developer(firstName, lastName, age, country, programmingLanguages)

class FrontendDeveloper(
    firstName: String,
    lastName: String,
    age: Int,
    country: String,
    programmingLanguages: List<String>,
    val frontendFramework: String
) : Developer(firstName, lastName, age, country, programmingLanguages)

fun countLanguages(developers : List<Developer>) : Map<String,Int>{
    return developers.flatMap { it.programmingLanguages}
        .groupingBy { it }
        .eachCount()
}

fun averageAgeByLanguage(developers : List<Developer>) : Map<String,Double> {
    return developers.flatMap { Developer->Developer.programmingLanguages.map { it to Developer.getAge_() } }
        .groupBy ( {it.first}, {it.second} )
        .mapValues{(_,ages) -> ages.average()}
}

fun countLanguagesWithoutGrouping(developers: List<Developer>): Map<String, Int> {
    val languageCount = mutableMapOf<String, Int>()
    for (developer in developers) {
        for (language in developer.programmingLanguages) {
            languageCount[language] = languageCount.getOrDefault(language, 0) + 1
        }
    }
    return languageCount
}

fun averageAgeByLanguageWithoutGrouping(developers: List<Developer>): Map<String, Double> {
    val languageAgeMap = mutableMapOf<String, MutableList<Int>>()
    for (developer in developers) {
        for (language in developer.programmingLanguages) {
            languageAgeMap.getOrPut(language) { mutableListOf() }.add(developer.getAge_())
        }
    }
    return languageAgeMap.mapValues { (_, ages) -> ages.average() }
}

fun printDeveloperData(developers: List<Developer>) {
    for (developer in developers) {
        println("Ime i prezime: ${developer.firstName} ${developer.lastName}")
        println("Tip programera: ${if (developer is BackendDeveloper) "Backend" else "Frontend"}")
        println("Programsko znanje: ${developer.programmingLanguages.joinToString()}")
        when (developer) {
            is BackendDeveloper -> println("Backend framework: ${developer.backendFramework}")
            is FrontendDeveloper -> println("Frontend framework: ${developer.frontendFramework}")
        }
        println()
    }
}

fun main() {
    val developers = listOf(
        BackendDeveloper("Amila", "Residovic", 22, "Bosna i Hercegovina", listOf("Kotlin"), "Spring Boot"),
        BackendDeveloper("Ibrahim", "Selimovic", 22, "Bosna i Hercegovina", listOf("Java"), "Spring"),
        FrontendDeveloper("Emina", "Jusufovic", 22, "Bosna i Hercegovina", listOf("Kotlin"), "React"),
        FrontendDeveloper("Mujo", "Alic", 22, "Bosna i Hercegovina", listOf("JavaScript"), "Vue.js"),
        BackendDeveloper("Edin", "Drapic", 50, "Bosna i Hercegovina", listOf("Kotlin"), "Ktor")
    )

    printDeveloperData(developers)

    val languageCount = countLanguages(developers)
    println("Broj programera po jeziku: $languageCount")

    val averageAge = averageAgeByLanguage(developers)
    println("Prosječna starost po jeziku: $averageAge")

    val languageCountNoGrouping = countLanguagesWithoutGrouping(developers)
    println("Broj programera po jeziku (bez groupingBy): $languageCountNoGrouping")

    val averageAgeNoGrouping = averageAgeByLanguageWithoutGrouping(developers)
    println("Prosječna starost po jeziku (bez groupingBy): $averageAgeNoGrouping")
}

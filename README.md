# Technomancy Demo

A small NeoForge 1.21.1 example for mod authors building on Sky’s Ley Lines. Two currencies show how separate machines draw on the same nexus reserve while keeping separate balances.

| Machine | Use with an empty hand | Shapeless recipe |
| --- | --- | --- |
| Aether Converter | Converts 5 raw magic into 5,000 aether at the default rate | Copper block, amethyst shard, redstone dust |
| Aether Lamp | Spends 1,000 aether for a five-second light pulse | Redstone lamp, copper ingot, amethyst shard |
| Electric Converter | Converts 5 raw magic into 50,000 electric at the default rate | Iron block, copper ingot, redstone dust |
| Electric Lamp | Spends 10,000 electric for a five-second light pulse | Redstone lamp, copper ingot, iron ingot |

Install Sky’s Ley Lines and Technomancy Demo on the client and server. Open the **Technomancy Demo** Creative tab to find all four machines. If the tabs wrap to a second page, use the right arrow above them. Find a nexus with dowsing rods or `/skysleylines locate nexus`, then place the machines near the same crossing at a usable height.

Try running only the Aether Converter first. The Aether Lamp can now spend its currency while the Electric Lamp still needs electric. Run the Electric Converter to fund the second balance. Each converter draws from the same raw reserve, and an active lamp ignores repeat clicks during its pulse.

Use `/skysleylines account` near the machines or the operator Ley Meter in Sky’s Ley Lines 0.5 to inspect raw magic and both currency balances. Conversion follows the saved rate for each currency.
## Build and run

Use JDK 21 and the included Gradle wrapper. Download [Sky’s Ley Lines 0.5.4.121012 for NeoForge 1.21.1](https://edge.forgecdn.net/files/9453/30/SkysLeyLines-0.5.4.121012.jar) and put it in `libs/` in this checkout. The build expects `libs/SkysLeyLines-0.5.4.121012.jar` by default. On Linux or macOS, run `./gradlew` in place of `.\gradlew.bat`.

```powershell
.\gradlew.bat build
.\gradlew.bat eclipse verifyEclipseProductionClasspath
.\gradlew.bat runClient
```

If your copy of Sky’s Ley Lines is somewhere else, pass its path with `-PleyLinesJar=/path/to/SkysLeyLines.jar`. For Eclipse, import this folder as an existing Gradle project using the wrapper and JDK 21. Use the default `libs` location or set `leyLinesJar` in your Gradle user properties so Buildship can find it. The build produces separate player and sources JARs in `build/libs`. The player JAR declares Sky’s Ley Lines as a required mod and expects it to be installed separately.

The Gradle client and server tasks stage the configured dependency as skysleylines-dev.jar in their respective dev run's mods folder. Use one copy of Sky's Ley Lines per run.

After changing Eclipse's Gradle user home, refresh the Gradle project and run the Gradle task again. The build regenerates its launch classpaths from the selected cache.

Windows developers using the guarded GLFW runtime from Sky’s Ley Lines can pass its DLL path with `-PglfwLibrary` for `runClient`.

## Follow the power flow

- [TechnomancyDemo.java](src/main/java/zone/moddev/mc/technomancydemo/TechnomancyDemo.java) registers the paradigm and both currencies in common setup.
- [ConverterBlock.java](src/main/java/zone/moddev/mc/technomancydemo/ConverterBlock.java) calls `convertAt` with the converter’s position and reports the amount credited from the receipt.
- [LampBlock.java](src/main/java/zone/moddev/mc/technomancydemo/LampBlock.java) calls `spendAt` with the lamp’s position and starts its pulse after payment succeeds.

Both operations run on the server thread and identify the machine and the player who used it. If you fork the demo, choose your own mod ID, Java package and resource namespace, including unique paradigm, currency and consumer IDs.

[NeoForge 1.21.1 registration guide](https://docs.neoforged.net/docs/1.21.1/concepts/registries/) · [NeoGradle run dependencies](https://docs.neoforged.net/toolchain/docs/plugins/ng/)

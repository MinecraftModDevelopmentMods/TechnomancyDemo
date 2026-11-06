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

Use JDK 21 and the included Gradle wrapper. Gradle downloads Sky’s Ley Lines 0.5.5.121012 from MMD Maven for compilation and development runs. On Linux or macOS, run `./gradlew` in place of `.\gradlew.bat`.

```powershell
.\gradlew.bat build
.\gradlew.bat eclipse verifyEclipseProductionClasspath
.\gradlew.bat runClient
```

For Eclipse, import this folder as an existing Gradle project using the wrapper and JDK 21. To test a different local Ley Lines build, pass `-PleyLinesJar=/path/to/SkysLeyLines.jar` or set `leyLinesJar` in your Gradle user properties. The build produces separate player and sources JARs in `build/libs`. Install Sky’s Ley Lines alongside the demo when playing outside the development workspace.

The Gradle client and server tasks stage the dependency as `skysleylines-dev.jar` in each run’s `mods` folder. Use one copy of Sky’s Ley Lines per run.

After changing Eclipse's Gradle user home, refresh the Gradle project and run the Gradle task again. The build regenerates its launch classpaths from the selected cache.

Windows developers using the guarded GLFW runtime from Sky’s Ley Lines can pass its DLL path with `-PglfwLibrary` for `runClient`.

## Follow the power flow

- [TechnomancyDemo.java](src/main/java/zone/moddev/mc/technomancydemo/TechnomancyDemo.java) registers the paradigm and both currencies in common setup.
- [ConverterBlock.java](src/main/java/zone/moddev/mc/technomancydemo/ConverterBlock.java) calls `convertAt` with the converter’s position and reports the amount credited from the receipt.
- [LampBlock.java](src/main/java/zone/moddev/mc/technomancydemo/LampBlock.java) calls `spendAt` with the lamp’s position and starts its pulse after payment succeeds.

Both operations run on the server thread and identify the machine and the player who used it. If you fork the demo, choose your own mod ID, Java package and resource namespace, including unique paradigm, currency and consumer IDs.

[NeoForge 1.21.1 registration guide](https://docs.neoforged.net/docs/1.21.1/concepts/registries/) · [NeoGradle run dependencies](https://docs.neoforged.net/toolchain/docs/plugins/ng/)

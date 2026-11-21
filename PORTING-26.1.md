# AE2WTLib: Fabric 26.1.2 port

Unofficial port of AE2WTLib (AE2 Wireless Terminals, by mari_023, MIT) to Fabric on Minecraft 26.1.2. It starts
from this fork's NeoForge `26.1` branch (upstream release `26.1.3-beta` plus one commit, `9258e0d7`) and lives on
branch `26.1-fabric`.

## AE2 on Fabric

The AE2 team only releases for NeoForge on 26.1. This port runs on **Applied Energistics 2: Fabricated**
(Fatin-Ishraq, https://github.com/Fatin-Ishraq/ae2-fabricated), an unofficial Fabric port of AE2 26.1.13 that keeps
the mod id `ae2` and the `appeng.api` packages. It is the AE2 jar in the testing modpack.

- AE2 Fabricated is not on a public maven. Put `ae2-fabricated-26.1.13-beta.1+fabric.jar` in `libs/` (gitignored;
  path set by `ae2Jar` in `gradle.properties`). 26.1 is unobfuscated, so the jar is used as is.
- AE2 Fabricated keeps NeoForge-shaped stand-ins under `appeng.platform.neoforge.*` (transfer, capabilities,
  `DeferredRegister`, `PacketDistributor`, `ModConfigSpec`, `NeoForgeStreamCodecs`, `RegisterMenuScreensEvent`).
  The port uses them where AE2's API hands those types around, so most files only changed imports. They are AE2
  Fabricated internals: check them when updating AE2 Fabricated.
- AE2 Fabricated has no addon entrypoint and registers everything in the `AppEngBase` constructor, called from its
  client / dedicated server entrypoints. ae2wtlib registers from mixins at the end of the `AppEngBase` and
  `AppEngClient` constructors, so the order never depends on entrypoint order. ae2wtlib's hotkeys are registered
  before AE2 turns hotkeys into key mappings, as on NeoForge.

## Build

- Loom 1.17.20 (non-remapping), Gradle 9.5.1, Java 25, Fabric Loader 0.20.1, Fabric API 0.155.3+26.1.2. The Kotlin
  NeoForge build scripts are replaced by Groovy Loom scripts. `ae2wtlib_api` is its own Fabric mod, shipped
  jar-in-jar, as on NeoForge.
- Version: `<upstream release>+fabric.<port build>` (`upstream_version` and `port_build` in `gradle.properties`).
- Optional, compile only: JEI 29.47.0.109 (fabric API), REI 26.1.819 (fabric API, plus Architectury for its types),
  EMI (compiled against the Mojang-named NeoForge 1.21.1 API like AE2 Fabricated; the Fabric jar uses intermediary
  names), Cloth Config 26.1.154 and Mod Menu 18.0.2.
- `./gradlew :runServer` starts a dev server with `-Dae2wtlib.mixinAudit=true`, which runs
  `MixinEnvironment.audit()` once the server is up.

## NeoForge to Fabric

| NeoForge | Fabric port |
|---|---|
| `@Mod` classes `AE2wtlibForge`, `AE2wtlibClient`, API `AE2wtlibAPIEntrypoint` | `AE2wtlibFabric.init()` / `AE2wtlibClient.init()` from `AppEngBaseMixin` / `AppEngClientMixin`; the API's `register()` is called by ae2wtlib right after it adds its terminals |
| `RegisterEvent` (ITEM, MENU), `FMLCommonSetupEvent` | Called in the same order in `AE2wtlibFabric.init()` |
| `BuildCreativeModeTabContentsEvent` | `CreativeModeTabEvents.MODIFY_OUTPUT_ALL`; tab built with `FabricCreativeModeTab.builder()` |
| `RegisterPayloadHandlersEvent` | `PayloadTypeRegistry` + `ServerPlayNetworking` / `ClientPlayNetworking` receivers (already on the game thread) |
| `RegisterCapabilitiesEvent` energy items | AE2 Fabricated's `RegisterCapabilitiesEvent` stand-in, bridged to Team Reborn Energy `EnergyStorage.ITEM` |
| Attachment `ct_handler` | Fabric data attachment (not persistent, not synced, not copied on respawn), created on first access |
| `ModConfigSpec` + `ConfigurationScreen` | AE2 Fabricated's `ModConfigSpec` (same `config/ae2wtlib.toml` and `ae2wtlib-client.toml`); screen through Mod Menu + Cloth Config when installed |
| `LivingEntityUseItemEvent.Finish` | `LivingEntityMixin` wraps `finishUsingItem` in `completeUsingItem` |
| `PlayerInteractEvent.RightClickBlock` / `EntityInteractSpecific` | `UseBlockCallback` / `UseEntityCallback` (with a hit result), returning PASS |
| `ItemEntityPickupEvent.Pre` | `ItemEntityMixin` at the start of `playerTouch`, same default pickup checks |
| `ArrowNockEvent` / `ArrowLooseEvent` | `BowItemMixin` at the start of `use` / `releaseUsing` |
| `ClientTickEvent.Post` | `ClientTickEvents.END_CLIENT_TICK` |
| `InputEvent.MouseScrollingEvent` | `MouseHandlerMixin` at the start of `onScroll` (no screen open, same delta) |
| `IItemExtension#isNotReplaceableByPickAction` (wireless terminals return true) | `InventoryMixin` in `getSuitableHotbarSlot` and the Stow hotkey use `PickAction`, which calls the terminal's method and otherwise keeps vanilla's `isEnchanted()` |
| `ItemStack#canEquip` (armor slots) | `LivingEntity#isEquippableInSlot`, which vanilla's `ArmorSlot` uses |
| Entity persistent data flag `PreventRemoteMovement` (magnet card skips such items) | Read from the item entity's tags (`/tag`), since Fabric has no shared persistent data |
| Access transformer (`ItemEntity.target`, `Slot.x/y`; NeoForge's own `Player.closeContainer`) | `@Shadow` in `ItemEntityMixin`; `ae2wtlib_api.accesswidener` |
| `ModList` | `FabricLoader` |
| `@REIPluginClient`, `@JeiPlugin`, `@EmiEntrypoint` | `rei_client`, `jei_mod_plugin`, `emi` entrypoints |

The upstream mixins carry over unchanged, except `ServerPlayerMixin`: the local captured in
`ServerPlayer.drop(boolean)` is `removed` in vanilla (NeoForge's patch renames it `selected`).

Nothing was dropped. Curios support is commented out on the NeoForge 26.1 branch too (upstream FIXME), so there is
no Trinkets/Accessories slot support either.

## Log

- 2026-11-21: port created. Both projects compile with no errors. A dev dedicated server (AE2 Fabricated
  26.1.13-beta.1, Fabric API 0.155.3) boots with ae2wtlib loaded. The mixin audit passes, and an export of the
  transformed classes shows every common mixin applied (`AEItems`, `AppEngBase`, `ServerPlayerGameMode`,
  `ServerPlayer`, `ServerGamePacketListenerImpl`, `LivingEntity`, `ItemEntity`, `BowItem`, `Inventory`). Client
  mixin targets (`Gui.extractSlot` / `itemDecorations`, `MouseHandler.onScroll`, `WidgetContainer.compositeWidgets`,
  `AppEngClient`) were checked against the 26.1.2 bytecode with javap. Not yet run in a client.
- 2026-11-21: the `v26.1-testing` client (full modpack) loads to the title screen with no ae2wtlib errors, and the
  user tried it in game and reported it working. Released as build 1 (`v26.1.3-beta-mc26.1.2-fabric.1`, GitHub
  pre-release).

## TODO

- [ ] Finish the in-game checklist: `Documents/git/port-testing/ae2wtlib.md`.

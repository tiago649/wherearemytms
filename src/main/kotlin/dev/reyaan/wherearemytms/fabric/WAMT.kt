package dev.reyaan.wherearemytms.fabric

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.item.group.CobblemonItemGroups
import dev.reyaan.wherearemytms.fabric.block.tmmachine.TMMachineBlock
import dev.reyaan.wherearemytms.fabric.block.tmmachine.TMMachineBlockEntity
import dev.reyaan.wherearemytms.fabric.config.WAMTConfigHandler
import dev.reyaan.wherearemytms.fabric.config.WAMTConfigObject
import dev.reyaan.wherearemytms.fabric.item.MoveTransferItem
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.enums.DoubleBlockHalf
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.io.File

object WAMT : ModInitializer {

    const val MOD_ID = "wherearemytms"

    fun id(path: String): Identifier {
        return Identifier.of(MOD_ID, path)
    }

    var POKEMON_HM = MoveTransferItem(Item.Settings().maxCount(16), true, "title.wherearemytms.hm")
    var POKEMON_TM = MoveTransferItem(Item.Settings().maxCount(16), false, "title.wherearemytms.tm")

    val TM_MACHINE: Block = TMMachineBlock(
        FabricBlockSettings.copy(Blocks.SMITHING_TABLE)
            .strength(3.0f)
            .hardness(4.0f)
            .nonOpaque()
            .luminance { state ->
                if (state.get(TMMachineBlock.HALF) == DoubleBlockHalf.LOWER) 5 else 0
            }
    )

    val TM_MACHINE_ITEM = BlockItem(TM_MACHINE, Item.Settings())

    val TM_MACHINE_BLOCK_ENTITY: BlockEntityType<TMMachineBlockEntity> = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        id("move_machine_block_entity"),
        FabricBlockEntityTypeBuilder.create(
            { pos: BlockPos, state: BlockState ->
                TMMachineBlockEntity(pos, state)
            },
            TM_MACHINE
        ).build()
    )

    val TM_MACHINE_MOVE_SELECT_PACKET_ID = id("packet.wherearemytms.close")

    lateinit var config: WAMTConfigObject

    override fun onInitialize() {

        config = WAMTConfigHandler(
            File(FabricLoader.getInstance().configDir.toString() + "/wherearemytms.json")
        ).init()

        registerTMs()
        registerMachine()

        ItemGroupEvents.modifyEntriesEvent(CobblemonItemGroups.BLOCKS_KEY)
            .register { content: FabricItemGroupEntries ->
                content.addAfter(
                    CobblemonItems.HEALING_MACHINE,
                    TM_MACHINE_ITEM
                )
            }

        ItemGroupEvents.modifyEntriesEvent(CobblemonItemGroups.CONSUMABLES_KEY)
            .register { content: FabricItemGroupEntries ->
                content.addAfter(
                    CobblemonItems.RARE_CANDY,
                    POKEMON_TM
                )
                content.addAfter(
                    POKEMON_TM,
                    POKEMON_HM
                )
            }

        WAMTNetwork.serverListeners()
    }

    fun registerMachine() {

        Registry.register(
            Registries.BLOCK,
            id("move_machine"),
            TM_MACHINE
        )

        Registry.register(
            Registries.ITEM,
            id("move_machine"),
            TM_MACHINE_ITEM
        )
    }

    fun registerTMs() {

        Registry.register(
            Registries.ITEM,
            id("blank_hm"),
            POKEMON_HM
        )

        Registry.register(
            Registries.ITEM,
            id("blank_tm"),
            POKEMON_TM
        )
    }
}

package crazypants.enderio.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import crazypants.enderio.EnderIO;
import crazypants.enderio.api.tool.IConduitControl;
import crazypants.enderio.conduit.ConduitDisplayMode;
import crazypants.enderio.item.PacketMagnetState.SlotType;
import crazypants.enderio.item.darksteel.DarkSteelController;
import crazypants.enderio.item.darksteel.DarkSteelItems;
import crazypants.enderio.item.darksteel.PacketUpgradeState;
import crazypants.enderio.item.darksteel.SoundDetector;
import crazypants.enderio.item.darksteel.upgrade.JumpUpgrade;
import crazypants.enderio.item.darksteel.upgrade.SoundDetectorUpgrade;
import crazypants.enderio.item.darksteel.upgrade.SpeedUpgrade;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.thaumcraft.GogglesOfRevealingUpgrade;
import crazypants.util.BaublesUtil;

import static crazypants.enderio.item.darksteel.DarkSteelItems.itemMagnet;

public class KeyTracker {

  public static final KeyTracker instance = new KeyTracker();
  
  static {
    FMLCommonHandler.instance().bus().register(instance);
  }
  
  private KeyBinding glideKey;  
  private boolean isGlideActive = false;
  
  private KeyBinding soundDetectorKey;  
  private boolean isSoundDectorActive = false;
  
  private KeyBinding nightVisionKey;  
  private boolean isNightVisionActive = false;
  
  private KeyBinding stepAssistKey;  
  private boolean isStepAssistActive = true;
  
  private KeyBinding speedKey;  
  private boolean isSpeedActive = true;
  
  
  private KeyBinding gogglesKey;  
  
  private KeyBinding yetaWrenchMode;  
  
  private KeyBinding magnetKey;
  
  public KeyTracker() {
    glideKey = new KeyBinding(EnderIO.lang.localize("keybind.glidertoggle"), Keyboard.KEY_G, EnderIO.lang.localize("category.darksteelarmor"));
    ClientRegistry.registerKeyBinding(glideKey);
    soundDetectorKey = new KeyBinding(EnderIO.lang.localize("keybind.soundlocator"), Keyboard.KEY_L, EnderIO.lang.localize("category.darksteelarmor"));
    ClientRegistry.registerKeyBinding(soundDetectorKey);        
    nightVisionKey = new KeyBinding(EnderIO.lang.localize("keybind.nightvision"), Keyboard.KEY_P, EnderIO.lang.localize("category.darksteelarmor"));
    ClientRegistry.registerKeyBinding(nightVisionKey);
    gogglesKey = new KeyBinding(EnderIO.lang.localize("keybind.gogglesofrevealing"), Keyboard.KEY_R, EnderIO.lang.localize("category.darksteelarmor"));
    ClientRegistry.registerKeyBinding(gogglesKey);
    
    stepAssistKey = new KeyBinding(EnderIO.lang.localize("keybind.stepassist"), Keyboard.KEY_NONE, EnderIO.lang.localize("category.darksteelarmor"));
    ClientRegistry.registerKeyBinding(stepAssistKey);
    
    speedKey = new KeyBinding(EnderIO.lang.localize("keybind.speed"), Keyboard.KEY_NONE, EnderIO.lang.localize("category.darksteelarmor"));
    ClientRegistry.registerKeyBinding(speedKey);
    
    yetaWrenchMode = new KeyBinding(EnderIO.lang.localize("keybind.yetawrenchmode"), Keyboard.KEY_Y, EnderIO.lang.localize("category.tools"));
    ClientRegistry.registerKeyBinding(yetaWrenchMode);

    magnetKey = new KeyBinding(EnderIO.lang.localize("keybind.magnet"), Keyboard.CHAR_NONE, EnderIO.lang.localize("category.tools"));
    ClientRegistry.registerKeyBinding(magnetKey);
  }
  
  @SubscribeEvent
  public void onKeyInput(KeyInputEvent event) {   
    handleGlide();
    handleSoundDetector();
    handleNightVision();
    handleYetaWrench();
    handleGoggles();
    handleStepAssist();
    handleSpeed();
    handleMagnet();
  }

  private void handleMagnet() {
    if(magnetKey.isPressed()) {
      EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
      ItemStack[] inv = player.inventory.mainInventory;
      for (int i = 0; i < 9; i++) {
        if(inv[i] != null && inv[i].getItem() != null && inv[i].getItem() == itemMagnet) {
          boolean isActive = !ItemMagnet.isActive(inv[i]);
          PacketHandler.INSTANCE.sendToServer(new PacketMagnetState(SlotType.INVENTORY, i, isActive));
          return;
        }
      }

      IInventory baubles = BaublesUtil.instance().getBaubles(player);
      if(baubles != null) {
        for (int i = 0; i < baubles.getSizeInventory(); i++) {
          ItemStack stack = baubles.getStackInSlot(i);
          if(stack != null && stack.getItem() != null && stack.getItem() == itemMagnet) {
            boolean isActive = !ItemMagnet.isActive(inv[i]);
            PacketHandler.INSTANCE.sendToServer(new PacketMagnetState(SlotType.BAUBLES, i, isActive));
            return;
          }
        }
      }
    }
  }

  private void handleSpeed() {
    if(!SpeedUpgrade.isEquipped(Minecraft.getMinecraft().thePlayer)) {
      return;
    }
    if(speedKey.isPressed()) {      
      isSpeedActive = !isSpeedActive;
      String message;
      if(isSpeedActive) {
        message = EnderIO.lang.localize("darksteel.upgrade.speed.enabled");
      } else {
        message = EnderIO.lang.localize("darksteel.upgrade.speed.disabled");
      }
      Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentTranslation(message));
      DarkSteelController.instance.setSpeedActive(Minecraft.getMinecraft().thePlayer, isSpeedActive);
      PacketHandler.INSTANCE.sendToServer(new PacketUpgradeState(PacketUpgradeState.Type.SPEED, isSpeedActive));
    }
  }

  private void handleStepAssist() {
    if(!JumpUpgrade.isEquipped(Minecraft.getMinecraft().thePlayer)) {
      return;
    }
    if(stepAssistKey.isPressed()) {      
      isStepAssistActive = !isStepAssistActive;
      String message;
      if(isStepAssistActive) {
        message = EnderIO.lang.localize("darksteel.upgrade.stepAssist.enabled");
      } else {
        message = EnderIO.lang.localize("darksteel.upgrade.stepAssist.disabled");
      }
      Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentTranslation(message));
      DarkSteelController.instance.setStepAssistActive(Minecraft.getMinecraft().thePlayer, isStepAssistActive);
      PacketHandler.INSTANCE.sendToServer(new PacketUpgradeState(PacketUpgradeState.Type.STEP_ASSIST, isStepAssistActive));
    }
    
  }

  private void handleGoggles() {
    EntityPlayer player = Minecraft.getMinecraft().thePlayer;
    if(!GogglesOfRevealingUpgrade.isUpgradeEquipped(player)){
      return;
    }
    if(gogglesKey.isPressed()) {      
      DarkSteelItems.itemDarkSteelHelmet.setGogglesUgradeActive(!DarkSteelItems.itemDarkSteelHelmet.isGogglesUgradeActive());
    }
    
  }

  private void handleYetaWrench() {
    if(!yetaWrenchMode.isPressed()) {
      return;
    }
    EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
    ItemStack equipped = player.getCurrentEquippedItem();
    if(equipped == null) {
      return;
    }
    if(equipped.getItem() instanceof IConduitControl) {
      ConduitDisplayMode curMode = ConduitDisplayMode.getDisplayMode(equipped);
      if(curMode == null) {
        curMode = ConduitDisplayMode.ALL;
      }
      ConduitDisplayMode newMode = curMode.next();
      ConduitDisplayMode.setDisplayMode(equipped, newMode);
      PacketHandler.INSTANCE.sendToServer(new YetaWrenchPacketProcessor(player.inventory.currentItem, newMode));
    } else if(equipped.getItem() == EnderIO.itemConduitProbe) {
      
      int newMeta = equipped.getItemDamage() == 0 ? 1 : 0;
      equipped.setItemDamage(newMeta);
      PacketHandler.INSTANCE.sendToServer(new PacketConduitProbeMode());   
      player.swingItem();
      
    }
    
        
  }

  private void handleSoundDetector() {
    if(!isSoundDetectorUpgradeEquipped(Minecraft.getMinecraft().thePlayer)) {
      SoundDetector.instance.setEnabled(false);
      return;
    }
    if(soundDetectorKey.isPressed()) {      
      isSoundDectorActive = !isSoundDectorActive;
      String message;
      if(isSoundDectorActive) {
        message = EnderIO.lang.localize("darksteel.upgrade.sound.enabled");
      } else {
        message = EnderIO.lang.localize("darksteel.upgrade.sound.disabled");
      }
      Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentTranslation(message));
      SoundDetector.instance.setEnabled(isSoundDectorActive);
    }
    
  }

  private void handleGlide() {
    if(!DarkSteelController.instance.isGliderUpgradeEquipped(Minecraft.getMinecraft().thePlayer)) {
      return;
    }
    if(glideKey.isPressed()) {      
      isGlideActive = !isGlideActive;
      String message;
      if(isGlideActive) {
        message = EnderIO.lang.localize("darksteel.upgrade.glider.enabled");
      } else {
        message = EnderIO.lang.localize("darksteel.upgrade.glider.disabled");
      }
      Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentTranslation(message));
      DarkSteelController.instance.setGlideActive(Minecraft.getMinecraft().thePlayer, isGlideActive);
      PacketHandler.INSTANCE.sendToServer(new PacketUpgradeState(PacketUpgradeState.Type.GLIDE, isGlideActive));
    }
  }
  
  private void handleNightVision() {
    EntityPlayer player = Minecraft.getMinecraft().thePlayer;
    if(!DarkSteelController.instance.isNightVisionUpgradeEquipped(player)){
      isNightVisionActive = false;
      return;
    }
    if(nightVisionKey.isPressed()) {      
      isNightVisionActive = !isNightVisionActive;
      if(isNightVisionActive) {
        player.worldObj.playSound(player.posX, player.posY, player.posZ, EnderIO.MODID + ":ds.nightvision.on", 0.1f, player.worldObj.rand.nextFloat() * 0.4f - 0.2f + 1.0f, false);
      } else {
        player.worldObj.playSound(player.posX, player.posY, player.posZ, EnderIO.MODID + ":ds.nightvision.off", 0.1f, 1.0f, false);
      }
      DarkSteelController.instance.setNightVisionActive(isNightVisionActive);      
    }
  }

  public boolean isGlideActive() {
    return isGlideActive;
  }   
    
  public boolean isSoundDetectorUpgradeEquipped(EntityClientPlayerMP player) {
    ItemStack helmet = player.getEquipmentInSlot(4);
    SoundDetectorUpgrade upgrade = SoundDetectorUpgrade.loadFromItem(helmet);
    if(upgrade == null) {
      return false;
    }
    return true;
  }
  
  public KeyBinding getYetaWrenchMode() {
    return yetaWrenchMode;
  }
}

package moveplus.forge;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

import moveplus.config.MovePlusCfg;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import CoroUtil.OldUtil;
import CoroUtil.util.CoroUtilEntity;

public class ClientTicker {
	
	public static int lastKey = 0;
    public static int[] keys;
    public static long[] keyTimes = new long[] {0L, 0L, 0L, 0L, 0L};
    public static boolean[] secondPress = new boolean[] {false, false, false, false, false};
    public static boolean dodged;
    public static boolean doubleJumped;
    public static int currentJumpCount = 0;
    public static long lastDodgeTime;
    public static long lastBoostDodgeTime;
    public static boolean speedJumped;
    public static long landTime;
    public static boolean lastTickLanded;
    public static long tickCount = 0L;
    public static long timeCount = 0L;
    public static long prevTimeCount = 0L;
    public static boolean toggleKeyPressed = false;
    public static boolean dodgeToggle = true;
    
    public static boolean superMagicProperty = true;
    public static boolean ingui = false;

    public static boolean wasSprinting = false;
    public static int camIndex = 0;
    public static int deathX = -1;
    public static int deathY = -1;
    public static int deathZ = -1;
    public static int targX = 0;
    public static int targY = 0;
    public static int targZ = 0;
    public static int savedX[];
    public static int savedY[];
    public static int savedZ[];
    public static PathEntity path;
    public static boolean pathFollowing = false;
    public static long lastWorldTime;
    public static boolean coordToggle = false;
    public static boolean wallOfPainToggle = false;
    public static NBTTagCompound gameData = null;

    //keep at bottom of var list
    public static boolean devFeatures = false;
    public static boolean camFeatures = true;
    public static int exaustCounter;
    public static boolean badCoro = false;
    public static Vec3 lastGroundVel = null;
    public static boolean wasOnGround = false;
    
    public static int timeout;
    public static String msg;
    public static int color;
    public static int defaultColor = 16777215;
    
    public static String infoMsg; 
    
    public static double prevMotionX;
    public static double prevMotionY;
    public static double prevMotionZ;
    public static boolean wasMoving = false;
	
	public static int homeX;
	public static int homeY;
	public static int homeZ;
	
	public static int coord_4_X;
	public static int coord_4_Y;
	public static int coord_4_Z;
	public static int coord_5_X;
	public static int coord_5_Y;
	public static int coord_5_Z;
	public static int coord_6_X;
	public static int coord_6_Y;
	public static int coord_6_Z;
	
	public static int hitCooldown;
	public static HashMap<EntityLivingBase, Long> lastHitTime = new HashMap();

	public static Minecraft mc;
	public static EntityPlayer theplayer;
	public static World worldRef;
	
	public static boolean needInitFirstTick = true;
	
    //public static Achievement boostdodged;

	public static void tickGame() {
		
	}
	
	public static void tickRenderScreen() {
		if (needInitFirstTick) {
			needInitFirstTick = false;
			initFirstTick();
		}
		tickUpdateMovement();
	}
	
	public static void tickRenderWorld() {
		
	}
	
	public static void initFirstTick() {

		mc = Minecraft.getMinecraft();
		keys = new int[] {mc.gameSettings.keyBindForward.getKeyCode(), mc.gameSettings.keyBindLeft.getKeyCode(), mc.gameSettings.keyBindBack.getKeyCode(), mc.gameSettings.keyBindRight.getKeyCode(), mc.gameSettings.keyBindJump.getKeyCode()};
		
	}
	
	/** MovePlus main methods **/

	public static void tickSafeties() {
    	
    	if (MovePlusCfg.knockbackResistAmount > 0D) {
        	
        	float speed = (float) Math.sqrt(theplayer.motionX * theplayer.motionX + theplayer.motionY * theplayer.motionY + theplayer.motionZ * theplayer.motionZ);
        	
            if (theplayer.hurtTime > 0) {
            	
            	theplayer.hurtTime = 0;
            	
            	if (MovePlusCfg.knockbackResistAmount == 1D) {
            		theplayer.motionX = prevMotionX;
                	theplayer.motionY = prevMotionY;
                	theplayer.motionZ = prevMotionZ;
            	} else {
            		theplayer.motionX = prevMotionX + (theplayer.motionX * (1D - Math.min(MovePlusCfg.knockbackResistAmount, 1D)));
                	theplayer.motionY = prevMotionY + (prevMotionY > 0.1D ? 0D : (theplayer.motionY * (1D - Math.min(MovePlusCfg.knockbackResistAmount, 1D))));
                	theplayer.motionZ = prevMotionZ + (theplayer.motionZ * (1D - Math.min(MovePlusCfg.knockbackResistAmount, 1D)));
            	}
            } else {
            	prevMotionX = theplayer.motionX;
            	prevMotionY = theplayer.motionY;
            	prevMotionZ = theplayer.motionZ;
            }
        }
    }
	
	public static void airControl(Entity var0) {
        if(MovePlusCfg.useAirControl && timeCount != worldRef.getWorldInfo().getWorldTime()) {
            float var1 = 0.0F;
            float var2 = 0.0F;

            if(Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
                ++var1;
            } else if(Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())) {
                --var2;
            } else if(Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
                --var1;
            } else if(Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())) {
                ++var2;
            }

            double var3 = Math.sqrt(var0.motionX * var0.motionX + var0.motionZ * var0.motionZ);

            if(MovePlusCfg.dolphinSkills && theplayer.isInWater()) {
                if(var3 < (double)MovePlusCfg.dolphinSpeedMax && var3 > 0.009999999776482582D && (var1 != 0.0F || var2 != 0.0F)) {
                    setRelVel2(theplayer, var2, (float)var0.motionY, var1, MovePlusCfg.dolphinSpeed);
                }
            } else if(MovePlusCfg.useAirControl && !theplayer.onGround && var3 < (double)MovePlusCfg.airControlSpeedMax && var3 > 0.009999999776482582D && (var1 != 0.0F || var2 != 0.0F)) {
                setRelVel(theplayer, var2, (float)var0.motionY, var1, MovePlusCfg.airControlSpeed);
            }
        }
    }
	
	public static void performMove(int var0, boolean var1, boolean var2) {
        performMove(var0, var1, var2, false);
    }

    public static void performMove(int var0, boolean var1, boolean var2, boolean var3) {
    	
    	//temp
    	//dodgeUpForce = 0.3F;
    	//dodgeForce = 1.2F;
    	
    	
        float var4 = MovePlusCfg.dodgeUpForce;
        float var5 = MovePlusCfg.dodgeForce;

        if(var1) {
            var4 += 0.4F;
        }

        if(var2) {
            var5 /= 2.0F;
        }

        float var6 = 0.0F;
        float var7 = 0.0F;

        if(var3) {
            if(Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
                ++var6;
            } else if(Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())) {
                --var7;
            } else if(Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
                --var6;
            } else {
                if(!Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())) {
                    return;
                }

                ++var7;
            }

            double var8 = Math.sqrt(theplayer.motionX * theplayer.motionX + theplayer.motionZ * theplayer.motionZ);
            setRelVel(theplayer, var7, 0.0F, var6, (float)var8 / 2.0F);
        } else {
            if(mc.gameSettings.keyBindForward.getKeyCode() == var0) {
                setRelVel(theplayer, 0.0F, var4, 1.0F, var5);
            } else if(mc.gameSettings.keyBindLeft.getKeyCode() == var0) {
                setRelVel(theplayer, -1.0F, var4, 0.0F + MovePlusCfg.fwForce, var5);
            } else if(mc.gameSettings.keyBindBack.getKeyCode() == var0) {
                setRelVel(theplayer, 0.0F, var4, -1.0F, var5);
            } else if(mc.gameSettings.keyBindRight.getKeyCode() == var0) {
                setRelVel(theplayer, 1.0F, var4, 0.0F + MovePlusCfg.fwForce, var5);
            } else if(mc.gameSettings.keyBindJump.getKeyCode() == var0) {
                setRelVel(theplayer, var7, MovePlusCfg.doubleJumpForce, var6, 0.0F);
            }
        }
    }

    public static void setRelVel(Entity var0, float var1, float var2, float var3, float var4) {
        float var5 = 10.0F;
        float var6 = 0.0F;
        float var7 = var0.prevRotationYaw + (var0.rotationYaw - var0.prevRotationYaw) * var5;
        int var8 = (int)Math.floor((double)(var7 / 360.0F) + 0.5D);
        var7 = var7 - (float)var8 * 360.0F + 270.0F;

        if(var3 <= 0.0F && var3 < 0.0F) {
            var7 += 180.0F;
        }

        if(var1 > 0.0F) {
            var7 += 90.0F - var3 * 10.0F;
        } else if(var1 < 0.0F) {
            var7 += 270.0F + var3 * 10.0F;
        }

        float var9 = MathHelper.cos(-var7 * 0.01745329F - 3.141593F);
        float var10 = MathHelper.sin(-var7 * 0.01745329F - 3.141593F);
        float var11 = -MathHelper.cos(-var6 * 0.01745329F - 0.7853982F);
        float var12 = MathHelper.sin(-var6 * 0.01745329F - 0.7853982F);
        float var13 = var9 * var11;
        float var15 = var10 * var11;

        if(var1 == 0.0F && var3 == 0.0F) {
            setVel(var0, (float)var0.motionX / 2.0F, var2, (float)var0.motionZ / 2.0F);
        } else {
            setVel(var0, var13 * var4 * -1.0F, var2, var15 * var4);
        }
    }

    public static void setRelVel2(Entity var0, float var1, float var2, float var3, float var4) {
        float var5 = 10.0F;
        float var6 = var0.prevRotationPitch + (var0.rotationPitch - var0.prevRotationPitch) * var5;
        float var7 = var0.prevRotationYaw + (var0.rotationYaw - var0.prevRotationYaw) * var5;
        int var8 = (int)Math.floor((double)(var7 / 360.0F) + 0.5D);
        var7 = var7 - (float)var8 * 360.0F + 270.0F;
        var6 = var6 - (float)var8 * 360.0F + 315.0F;

        if(var3 <= 0.0F && var3 < 0.0F) {
            var7 += 180.0F;
        }

        if(var1 > 0.0F) {
            var7 += 90.0F - var3 * 10.0F;
        } else if(var1 < 0.0F) {
            var7 += 270.0F + var3 * 10.0F;
        }

        float var9 = MathHelper.cos(-var7 * 0.01745329F - 3.141593F);
        float var10 = MathHelper.sin(-var7 * 0.01745329F - 3.141593F);
        float var11 = -MathHelper.cos(-var6 * 0.01745329F - 0.7853982F);
        float var12 = MathHelper.sin(-var6 * 0.01745329F - 0.7853982F);
        float var13 = var9 * var11;
        float var15 = var10 * var11;

        if(var1 == 0.0F && var3 == 0.0F) {
            setVel(var0, (float)var0.motionX / 2.0F, var2, (float)var0.motionZ / 2.0F);
        } else {
            setVel(var0, var13 * var4 * -1.0F, var12 * var4 * 5.0F, var15 * var4);
        }
    }

    public static void setVel(Entity var0, float var1, float var2, float var3) {
        var0.motionX += (double)var1;
        var0.motionY = (double)var2;
        var0.motionZ += (double)var3;
    }
    
    public static void pathFollow() {
    	
        Vec3 var5 = path.getPosition(theplayer);
        double var6 = (double)(theplayer.width * 1.2F);

        while(var5 != null && var5.squareDistanceTo(theplayer.posX, var5.yCoord, theplayer.posZ) < var6 * var6) {
            path.incrementPathIndex();

            if(path.isFinished()) {
                var5 = null;
                path = null;
            } else {
                var5 = path.getPosition(theplayer);
            }
        }

        int var21 = MathHelper.floor_double(theplayer.boundingBox.minY + 0.5D);
        float angle = 0F;

        if(var5 != null) {
            double var8 = var5.xCoord - theplayer.posX;
            double var10 = var5.zCoord - theplayer.posZ;
            double var12 = var5.yCoord - (double)var21;
            float var14 = (float)(Math.atan2(var10, var8) * 180.0D / 3.1415927410125732D) - 90.0F;
            float var15 = var14 - theplayer.rotationYaw;

            for(angle = 0.28F; var15 < -180.0F; var15 += 360.0F) {
                ;
            }

            while(var15 >= 180.0F) {
                var15 -= 360.0F;
            }

            if(var15 > 30.0F) {
                var15 = 30.0F;
            }

            if(var15 < -30.0F) {
                var15 = -30.0F;
            }

            theplayer.rotationYaw += var15;

            if(var12 > 0.0D || theplayer.handleWaterMovement() || theplayer.handleLavaMovement()) {
                theplayer.setJumping(true);
            }

            if ((Boolean) OldUtil.getPrivateValueSRGMCP(EntityLivingBase.class, theplayer, "field_70703_bu", "isJumping")) {
                //this.A
                if (theplayer.onGround) {
                    performMove(keys[4], false, false);
                }
            }
        }

        //float speed = theplayer.cg;
        //theplayer.cg = 0.1F;
        //theplayer.moveEntityWithHeading(0F,angle);
        if (theplayer.onGround) {
        	theplayer.moveFlying(0F, angle, 0.15F);
        } else {
        	theplayer.moveFlying(0F, angle, 0.05F);
        }
        
        
        
        //theplayer.moveEntityWithHeading(0F,angle);
        //theplayer.cg = speed;
    }
	
	public static String getWorldSavePath() {
		//return "moveplus/" + theplayer.worldObj.getWorldInfo().getWorldName() + "/";
		return "/";
	}
	
	public static void tryExaust(int inc) {
    
    	if (MovePlusCfg.useStamina) {
	    	exaustCounter += inc;
	    	/*if (exaustCounter >= 5) {
	    		exaustCounter = 0;
	    		theplayer.addExhaustion(1F);
	    	}*/
    	}
    }
	
	public static void checkKey(int var0) {
    	
    	//test
    	//autoJump = true;
    	if (MovePlusCfg.autoJump && theplayer.onGround && nearWall(theplayer) && Keyboard.isKeyDown(keys[0])) {
    		theplayer.jump();
    	}
    	
    	if (wasSprinting) {
    		theplayer.setSprinting(true);
    		if (!Keyboard.isKeyDown(keys[0])) {
    			wasSprinting = false;
    		}
    	}
    	
    	
        if(MovePlusCfg.useSpeedJump && var0 == 4 && Keyboard.isKeyDown(keys[var0]) && !speedJumped && tickCount - landTime < 3L) {
            double var1 = theplayer.motionY;
            double var3 = Math.sqrt(theplayer.motionX * theplayer.motionX + theplayer.motionZ * theplayer.motionZ);

            if(var3 < (double)MovePlusCfg.speedJumpMax) {
            	if (!MovePlusCfg.useStamina || theplayer.getFoodStats().getFoodLevel() > 3) {
            		performMove(keys[var0], false, false, true);
            		tryExaust(1);
            	}
            }

            theplayer.motionY = var1;
            speedJumped = true;
            keyTimes[var0] = tickCount - 500L;
        }
        
        if (var0 == 4 && !theplayer.onGround) {
        	//System.out.println("not on ground, second press: " + secondPress[var0]);
        	//secondPress[var0] = false;
        }

        if(MovePlusCfg.useDoubleJump && var0 == 4 && secondPress[var0] && Keyboard.isKeyDown(keys[var0])) {
            if(!theplayer.onGround && (!doubleJumped || currentJumpCount < MovePlusCfg.maxExtraJumps)) {
                if((!doubleJumped || currentJumpCount < MovePlusCfg.maxExtraJumps)) {
                    if (!MovePlusCfg.useStamina || theplayer.getFoodStats().getFoodLevel() > 3) {
                    	//System.out.println("double jump!");
                    	performMove(keys[var0], false, false);
                    	tryExaust(5);
                    }
                }

                if (!devFeatures) {
                	doubleJumped = true;
                	currentJumpCount++;
                }
            }

            secondPress[var0] = false;
            keyTimes[var0] = tickCount - 500L;
        }
        
        

        if(tickCount - keyTimes[var0] < (long)MovePlusCfg.dodgeMinDelayTime && secondPress[var0] && Keyboard.isKeyDown(keys[var0])) {
            if(System.currentTimeMillis() > lastBoostDodgeTime && MovePlusCfg.useBoostDodge && Keyboard.isKeyDown(Keyboard.getKeyIndex("SPACE")) && keyTimes[var0] - keyTimes[4] - 500L < 5L && nearWall(theplayer)) {
            	lastBoostDodgeTime = System.currentTimeMillis() + MovePlusCfg.boostDodgeDelay;
            	if (!MovePlusCfg.useStamina || theplayer.getFoodStats().getFoodLevel() > 3) {
            		//theplayer.triggerAchievement(boostdodged);
            		performMove(keys[var0], true, false);
            		tryExaust(20);
            	}
            } else if(dodgeToggle && ((theplayer.onGround && System.currentTimeMillis() > lastDodgeTime) || (nearWall(theplayer)) && MovePlusCfg.useWallDodge)) {
            	lastDodgeTime = System.currentTimeMillis() + MovePlusCfg.dodgeDelay;
            	if (!MovePlusCfg.useStamina || theplayer.getFoodStats().getFoodLevel() > 3) {
            		performMove(keys[var0], false, false);
            		tryExaust(5);
            	}
            } else if(!dodged && !doubleJumped) {
            	if (MovePlusCfg.useGroundDodge) {
	            	if (System.currentTimeMillis() > lastDodgeTime && dodgeToggle) {
	            		lastDodgeTime = System.currentTimeMillis() + MovePlusCfg.dodgeDelay;
		            	if (!MovePlusCfg.useStamina || theplayer.getFoodStats().getFoodLevel() > 3) {
		            		performMove(keys[var0], false, true);
		            		tryExaust(5);
		            	}
	            	}
            	}

            	//sprint persist addition
            	if (Keyboard.isKeyDown(keys[0])) wasSprinting = true;
            }

            if (!devFeatures) {
            	dodged = true;
            	theplayer.onGround = false;
            }

            secondPress[var0] = false;
            keyTimes[var0] = tickCount - 500L;
        }

        if(!Keyboard.isKeyDown(keys[var0]) && !secondPress[var0]) {
            secondPress[var0] = true;
        }

        if(Keyboard.isKeyDown(keys[var0]) && secondPress[var0]) {
            secondPress[var0] = false;
            lastKey = keys[var0];
            keyTimes[var0] = tickCount;
        }
    }
	
	public static void writeGameNBT() {
    	//System.out.println("Saving ZC game..." + zcLevel.map_coord_minX);
    	gameData = new NBTTagCompound();
    	try {
    		
    		//Player data
    		
    		//Level position and name data, rest should be done by level
    		//gameData.setString("levelName", mapMan.curLevel);
    		//gameData.setString("texturePack", mapMan.texturePack);
    		gameData.setInteger("homeX", homeX);
    		gameData.setInteger("homeY", homeY);
    		gameData.setInteger("homeZ", homeZ);
    		
    		gameData.setInteger("coord_4_X", coord_4_X);
    		gameData.setInteger("coord_4_Y", coord_4_Y);
    		gameData.setInteger("coord_4_Z", coord_4_Z);
    		gameData.setInteger("coord_5_X", coord_5_X);
    		gameData.setInteger("coord_5_Y", coord_5_Y);
    		gameData.setInteger("coord_5_Z", coord_5_Z);
    		gameData.setInteger("coord_6_X", coord_6_X);
    		gameData.setInteger("coord_6_Y", coord_6_Y);
    		gameData.setInteger("coord_6_Z", coord_6_Z);
    		
    		
    		gameData.setInteger("deathX", deathX);
    		gameData.setInteger("deathY", deathY);
    		gameData.setInteger("deathZ", deathZ);
    		
    		String saveFolder = getWorldSavePath();
    		
    		//Write out to file
    		FileOutputStream fos = new FileOutputStream(saveFolder + "MovePlus.dat");
	    	CompressedStreamTools.writeCompressed(gameData, fos);
	    	fos.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    
    public static void readGameNBT() {
    	gameData = null;
		try {
			
			String saveFolder = getWorldSavePath();
			gameData = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "MovePlus.dat"));
			
			homeX = gameData.getInteger("homeX");
			homeY = gameData.getInteger("homeY");
			homeZ = gameData.getInteger("homeZ");
			
			coord_4_X = gameData.getInteger("coord_4_X");
			coord_4_Y = gameData.getInteger("coord_4_Y");
			coord_4_Z = gameData.getInteger("coord_4_Z");
			coord_5_X = gameData.getInteger("coord_5_X");
			coord_5_Y = gameData.getInteger("coord_5_Y");
			coord_5_Z = gameData.getInteger("coord_5_Z");
			coord_6_X = gameData.getInteger("coord_6_X");
			coord_6_Y = gameData.getInteger("coord_6_Y");
			coord_6_Z = gameData.getInteger("coord_6_Z");
			
			
			deathX = gameData.getInteger("deathX");
			deathY = gameData.getInteger("deathY");
			deathZ = gameData.getInteger("deathZ");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }
	
	public static boolean nearWall(Entity var0) {
        return var0.worldObj.getCollidingBoundingBoxes(var0, var0.boundingBox.expand(0.2D, 0.0D, 0.2D)).size() > 0;
    }
	
	public static void displayMessage(String var0, int var1) {
        msg = var0;
        timeout = 85;
        color = var1;
    }

    public static void displayMessage(String var0) {
        displayMessage(var0, defaultColor);
    }
	
	public static void tickUpdateMovement() {
		
		if (theplayer != null) {
    		if (theplayer.getHealth() <= 0) {
    			deathX = (int)theplayer.posX;
    			deathY = (int)theplayer.posY;
    			deathZ = (int)theplayer.posZ;
    			writeGameNBT();
    		}
    	}
    	if (worldRef != mc.theWorld) {
    		exaustCounter = 0;
    	}
		
		theplayer = mc.thePlayer;
		worldRef = mc.theWorld;
		
		boolean var2 = false;
		
		if (theplayer == null || worldRef == null) return;
		
		if(theplayer != null && !var2 && ((worldRef != null))) {
			
			tickSafeties();
			
			if ((mc.currentScreen instanceof GuiContainer || (mc.currentScreen instanceof GuiChat)) && pathFollowing && path != null && lastWorldTime != worldRef.getWorldInfo().getWorldTime()) {
                displayMessage("Pathfinding");
                pathFollow();
                lastWorldTime = worldRef.getWorldInfo().getWorldTime();
            }
			
            boolean var3 = nearWall(theplayer);
            
            if (!ingui && mc.currentScreen == null) {
            	
	            if(Keyboard.isKeyDown(Keyboard.getKeyIndex(MovePlusCfg.toggleKey))) {
	                if(!toggleKeyPressed) {
	                    dodgeToggle = !dodgeToggle;
	                    toggleKeyPressed = true;
	                    displayMessage("Dodging: " + (dodgeToggle?"enabled":"disabled"));
	                }
	            } else if(Keyboard.isKeyDown(Keyboard.getKeyIndex("M"))) {
	                if(!toggleKeyPressed) {
	                    //mc.h.a(new ade(ud.l.H, 30*20, 10));
	                    //mc.h.a(new ade(ud.p.H, 30*20, 10));
	                    coordToggle = !coordToggle;
	                    toggleKeyPressed = true;
	                    
	                    /*HashMap<String, Integer> entNames = new HashMap<String, Integer>();
	                    
	                    for (int var33 = 0; var33 < worldRef.loadedEntityList.size(); ++var33)
	                    {
	                        Entity ent = (Entity)worldRef.loadedEntityList.get(var33);
	                        
	                        int val = 0;
	                        
	                        
	                        
	                        if (entNames.containsKey(EntityList.getEntityString(ent))) {
	                        	val = entNames.get(EntityList.getEntityString(ent))+1;
	                        }
	                        entNames.put(EntityList.getEntityString(ent), val);
	                        
	                    }
	                    
                        Iterator it = entNames.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pairs = (Map.Entry)it.next();
                            System.out.println(pairs.getKey() + " = " + pairs.getValue());
                            it.remove();
                        }*/
	                    
	                    //listEntities();
	                    

	                }
	            } else if(devFeatures && Keyboard.isKeyDown(Keyboard.KEY_NUMPAD3)) {
	                if(!toggleKeyPressed) {
	                	wallOfPainToggle = !wallOfPainToggle;
	                    toggleKeyPressed = true;
	                    displayMessage("Wall of Pain: " + (wallOfPainToggle?"enabled":"disabled"));
	                }
	            } else if (camFeatures && Keyboard.isKeyDown(Keyboard.KEY_NUMPAD0)) {
	                if(!toggleKeyPressed) {
	                    try {
	                        mc.renderViewEntity = theplayer;
	                    } catch (Exception ex) {
	                        ex.printStackTrace();
	                    }
	
	                    camIndex = 0;
	                    toggleKeyPressed = true;
	                }
	            } else if (mc.currentScreen == null && camFeatures && Keyboard.isKeyDown(Keyboard.KEY_NUMPAD1)) {
	                if(!toggleKeyPressed && worldRef.playerEntities.size() > 1) {
	                    System.out.println(worldRef.playerEntities.size());
	
	                    //int tryIndex = camIndex;
	                    if (camIndex >= worldRef.playerEntities.size()) {
	                        camIndex = 0;
	                    }
	
	                    //System.out.println("index: " + camIndex);
	                    //System.out.println("name: " + ((EntityPlayer)worldRef.playerEntities.get(camIndex)).username);
	                    
	                    displayMessage("Viewing: " + (CoroUtilEntity.getName((EntityPlayer)worldRef.playerEntities.get(camIndex))));
	
	                    try {
	                        if (worldRef.playerEntities.get(camIndex) instanceof EntityPlayer) {
	                            mc.renderViewEntity = (EntityLivingBase)worldRef.playerEntities.get(camIndex);
	                        }
	                    } catch (Exception ex) {
	                        ex.printStackTrace();
	                    }
	
	                    camIndex++;
	                    toggleKeyPressed = true;
	                }
	            } else if (mc.currentScreen == null && camFeatures && Keyboard.isKeyDown(Keyboard.KEY_NUMPAD2)) {
	                if(!toggleKeyPressed && worldRef.playerEntities.size() > 1) {
	                    //System.out.println(worldRef.playerEntities.size());
	
	                    if (camIndex < 0) {
	                        camIndex = worldRef.playerEntities.size()-1;
	                    }
	                    
	                    displayMessage("Viewing: " + (CoroUtilEntity.getName((EntityPlayer)worldRef.playerEntities.get(camIndex))));
	
	                    try {
	                        if (worldRef.playerEntities.get(camIndex) instanceof EntityPlayer) {
	                            mc.renderViewEntity = (EntityPlayer)worldRef.playerEntities.get(camIndex);
	                        }
	                    } catch (Exception ex) {
	                        ex.printStackTrace();
	                    }
	
	                    camIndex--;
	                    toggleKeyPressed = true;
	                }
	            } else if (MovePlusCfg.pathFeatures && Keyboard.isKeyDown(Keyboard.KEY_NUMPAD4)) {
	                if(!toggleKeyPressed) {
	                    //System.out.println(worldRef.playerEntities.size());
	                    if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
	                    	coord_4_X = (int)theplayer.posX;
	                    	coord_4_Y = (int)theplayer.posY;
	                    	coord_4_Z = (int)theplayer.posZ;
	                    	
	                    	displayMessage("WP1 set to: " + coord_4_X + " - " + coord_4_Y + " - " + coord_4_Z);
	                		
	                		writeGameNBT();
	                	} else {
		                    targX = coord_4_X;
		                    targY = coord_4_Y;
		                    targZ = coord_4_Z;
		                    
		                    displayMessage("WP1 PF Targ: " + coord_4_X + " - " + coord_4_Y + " - " + coord_4_Z);
	                	}
	                    toggleKeyPressed = true;
	                }
	            } else if (MovePlusCfg.pathFeatures && Keyboard.isKeyDown(Keyboard.KEY_NUMPAD5)) {
	                if(!toggleKeyPressed) {
	                    //System.out.println(worldRef.playerEntities.size());
	                	if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
	                    	coord_5_X = (int)theplayer.posX;
	                    	coord_5_Y = (int)theplayer.posY;
	                    	coord_5_Z = (int)theplayer.posZ;
	                    	
	                    	displayMessage("WP2 set to: " + coord_5_X + " - " + coord_5_Y + " - " + coord_5_Z);
	                		
	                		writeGameNBT();
	                	} else {
		                    targX = coord_5_X;
		                    targY = coord_5_Y;
		                    targZ = coord_5_Z;
		                    
		                    displayMessage("WP2 PF Targ: " + coord_5_X + " - " + coord_5_Y + " - " + coord_5_Z);
	                	}
	                    toggleKeyPressed = true;
	                }
	            } else if (MovePlusCfg.pathFeatures && Keyboard.isKeyDown(Keyboard.KEY_NUMPAD6)) {
	                if(!toggleKeyPressed) {
	                    //System.out.println(worldRef.playerEntities.size());
	                	if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
	                    	coord_6_X = (int)theplayer.posX;
	                    	coord_6_Y = (int)theplayer.posY;
	                    	coord_6_Z = (int)theplayer.posZ;
	                    	
	                    	displayMessage("WP3 set to: " + coord_6_X + " - " + coord_6_Y + " - " + coord_6_Z);
	                		
	                		writeGameNBT();
	                	} else {
		                    targX = coord_6_X;
		                    targY = coord_6_Y;
		                    targZ = coord_6_Z;
		                    
		                    displayMessage("WP3 PF Targ: " + coord_6_X + " - " + coord_6_Y + " - " + coord_6_Z);
	                	}
	                    toggleKeyPressed = true;
	                }
	            /*} else if (MovePlusCfg.pathFeatures && Keyboard.isKeyDown(Keyboard.KEY_NUMPAD7)) {
	                if(!toggleKeyPressed) {
	                    //System.out.println(worldRef.playerEntities.size());
	                    targX = (int)theplayer.posX;
	                    targY = (int)theplayer.posY;
	                    targZ = (int)theplayer.posZ;
	                    toggleKeyPressed = true;
	                }
	            } else if (MovePlusCfg.pathFeatures && Keyboard.isKeyDown(Keyboard.KEY_NUMPAD8)) {
	                if(!toggleKeyPressed) {
	                    //System.out.println(worldRef.playerEntities.size());
	                    targX = (int)theplayer.posX;
	                    targY = (int)theplayer.posY;
	                    targZ = (int)theplayer.posZ;
	                    toggleKeyPressed = true;
	                }
	            } else if (MovePlusCfg.pathFeatures && Keyboard.isKeyDown(Keyboard.KEY_NUMPAD9)) {
	                if(!toggleKeyPressed) {
	                    //System.out.println(worldRef.playerEntities.size());
	                    targX = (int)theplayer.posX;
	                    targY = (int)theplayer.posY;
	                    targZ = (int)theplayer.posZ;
	                    toggleKeyPressed = true;
	                }*/
	            } else if (MovePlusCfg.pathFeatures && Keyboard.isKeyDown(Keyboard.KEY_HOME)) {
	                if(!toggleKeyPressed) {
	                    //System.out.println(worldRef.playerEntities.size());
	                	if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
	                		homeX = (int)theplayer.posX;
	                		homeY = (int)theplayer.posY;
	                		homeZ = (int)theplayer.posZ;
	                		
	                		displayMessage("Home set to: " + homeX + " - " + homeY + " - " + homeZ);
	                		
	                		writeGameNBT();
	                	} else {
		                    targX = homeX;
		                    targY = homeY;
		                    targZ = homeZ;
		                    
		                    displayMessage("Home PF Targ: " + homeX + " - " + homeY + " - " + homeZ);
	                	}
	                    toggleKeyPressed = true;
	                }
	            } else if (MovePlusCfg.pathFeatures && Keyboard.isKeyDown(Keyboard.KEY_END)) {
	                if(!toggleKeyPressed) {
	                    //System.out.println(worldRef.playerEntities.size());
	                    targX = deathX;
	                    targY = deathY;
	                    targZ = deathZ;
	                    
	                    displayMessage("Death PF Targ: " + deathX + " - " + deathY + " - " + deathZ);
	                    
	                    toggleKeyPressed = true;
	                }
	            } else if (MovePlusCfg.pathFeatures && Keyboard.isKeyDown(Keyboard.KEY_INSERT)) {
	                if(!toggleKeyPressed) {
	                    //System.out.println(worldRef.playerEntities.size());
	                	/*if (PFQueue.instance == null) {
	                		new PFQueue(theplayer.worldObj);
	                	}*/
	                	try {
	                		//path = PFQueue.instance.convertToPathEntity(PFQueue.instance.createEntityPathTo(theplayer, targX, targY, targZ, 256.0F));
	                	} catch (Exception ex) {
	                		ex.printStackTrace();
	                	}
	                	//if (path == null) {
	                		//System.out.println("PFQueue fail");
	                	path = worldRef.getEntityPathToXYZ(theplayer, targX, targY, targZ, 256.0F, true, true, true, true);
	                		//PathPointEx var11 = new PathPointEx(MathHelper.floor_float(theplayer.width + 1.0F), MathHelper.floor_float(theplayer.height + 1.0F), MathHelper.floor_float(theplayer.width + 1.0F));
	                		//path = PFQueue.instance.convertToPathEntity(PFQueue.instance.simplifyPath(PFQueue.instance.convertToPathEntityEx(path), var11));
	                	//}
	                    pathFollowing = true;
	                    toggleKeyPressed = true;
	                }
	            } else if (MovePlusCfg.pathFeatures && Keyboard.isKeyDown(Keyboard.KEY_DELETE)) {
	                if(!toggleKeyPressed) {
	                    //System.out.println(worldRef.playerEntities.size());
	                    pathFollowing = false;
	                    toggleKeyPressed = true;
	                }
	            } else if (devFeatures && Keyboard.isKeyDown(Keyboard.KEY_PERIOD)) {
	                if(!toggleKeyPressed) {
	                    int xx = (int)theplayer.posX+40;
	                    int yy = (int)theplayer.posY;
	                    int zz = (int)theplayer.posZ+40;
	                    int size = 20;
	
	                    for (int xxx = xx-size; xxx < xx+size; xxx++) {
	                        for (int yyy = yy; yyy < 128; yyy++) {
	                            for (int zzz = zz-size; zzz < zz+size; zzz++) {
	                                //worldRef.setBlockWithNotify(xxx, yyy, zzz, 3);
	                            }
	                        }
	                    }
	
	                    toggleKeyPressed = true;
	                }
	            } else {
	                toggleKeyPressed = false;
	            }
            }

            if (lastWorldTime != worldRef.getWorldInfo().getWorldTime()) {
            	if (pathFollowing && path != null) {
	                displayMessage("Pathfinding");
	                pathFollow();
            	}
            	
            	float range = 5F;
            	
            	//if (hitCooldown > 0) hitCooldown--;
                if (wallOfPainToggle/* && hitCooldown == 0*/) {
    	            List list = worldRef.getEntitiesWithinAABBExcludingEntity(theplayer, theplayer.boundingBox.expand(range, range, range));
    	
    	            for(int count = 0; count < list.size(); ++count) {
    	               Entity var5 = (Entity)list.get(count);
    	
    	               if(var5 instanceof EntityLivingBase && !(var5 instanceof EntityPlayer || var5 instanceof EntityVillager || var5 instanceof EntityAnimal) && ((EntityLivingBase) var5).hurtTime <= 0) {
    	
    	            	if (!lastHitTime.containsKey(((EntityLivingBase) var5)) || System.currentTimeMillis() - lastHitTime.get(((EntityLivingBase) var5)).longValue() > 250) {
    	            		//System.out.println(var5 + ": " + ((EntityLivingBase) var5).hurtTime);
        	               	//hitCooldown = 10;
        	               	lastHitTime.put(((EntityLivingBase) var5), System.currentTimeMillis());
        	               	mc.playerController.attackEntity(theplayer, var5);
    	            	}
    	               }
    	            }
                }
                
                if (exaustCounter > 0 && worldRef.getWorldInfo().getWorldTime() % 20 == 0) {
                	exaustCounter--;
	                //test
	                //p.moving = true;
	                //((EntityClientPlayerMP)theplayer).sendQueue.addToSendQueue(p);
	                
	                //((EntityClientPlayerMP)theplayer).sendQueue.addToSendQueue(new Packet11PlayerPosition(theplayer.motionX, -999D, -999D, theplayer.motionZ, true));
	                //for (int i = 0; i < 6; i++) {
	                ((EntityClientPlayerMP)theplayer).sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(theplayer.posX, theplayer.boundingBox.minY+0.1, theplayer.posY+0.1, theplayer.posZ, theplayer.rotationYaw, theplayer.rotationPitch, false/*theplayer.onGround*/));
	                //System.out.println("exaustCounter: " + exaustCounter);
	                //System.out.println(theplayer.getFoodStats().foodExhaustionLevel);
	                //}
	                //this.sendQueue.addToSendQueue(new Packet13PlayerLookMove(this.posX, this.boundingBox.minY, this.posY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround));
                }
                
                if (badCoro && worldRef.getWorldInfo().getWorldTime() % 20 == 0) {
                	
                	int range2 = 20;
                	
                	ChunkCoordinates best = null;
                	double closest = 9999F;
                	
                	for (int x = (int)theplayer.posX - range2/2; x < theplayer.posX + range2/2; x++) {
                		for (int y = (int)theplayer.posY - range2/4; y < theplayer.posY + range2/4; y++) {
                			for (int z = (int)theplayer.posZ - range2/2; z < theplayer.posZ + range2/2; z++) {
                				
                				/*int id = worldRef.getBlockId(x, y, z);
                				
                				if (id == Block.oreDiamond.blockID) {
                					
                					if (theplayer.getDistance(x, y, z) < closest) {
                						closest = theplayer.getDistance(x, y, z);
                						best = new ChunkCoordinates(x, y, z);
                					}
                				}*/
                				
                			}
                		}
                	}
                	
                	if (best != null) {
                		infoMsg = "at: " + best.posX + ", " + best.posY + ", " + best.posZ;
                	}
                }
                
                if (MovePlusCfg.airResistanceRemoval) {
	                if (theplayer.onGround) {
	                	if (wasOnGround) {
	                		lastGroundVel = Vec3.createVectorHelper(theplayer.motionX, theplayer.motionY, theplayer.motionZ);
	                	}
	                	wasOnGround = true;
	                } else {
	                	
	                	if (theplayer.capabilities.isCreativeMode && theplayer.capabilities.isFlying) {
	                		
	                		if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) ||
	                				Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()) || 
	                				Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()) || 
	                				Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())) {
	                			double curSpeed = Math.sqrt(theplayer.motionX * theplayer.motionX + theplayer.motionZ * theplayer.motionZ);
	                			if (curSpeed < 2F) {
	                				theplayer.motionX *= 1.1F;
	                        		theplayer.motionZ *= 1.1F;
	                			}
	                			
	                		}
	                		
	                		
	                	} else {
	                	
		                	if (lastGroundVel != null) {
			                	theplayer.motionX = (theplayer.motionX / 0.91F) * 0.95F;
			                	theplayer.motionZ = (theplayer.motionZ / 0.91F) * 0.95F;
			                	
			                	double lastGroundSpeed = Math.sqrt(lastGroundVel.xCoord * lastGroundVel.xCoord + lastGroundVel.zCoord * lastGroundVel.zCoord);
			                	double curSpeed = Math.sqrt(theplayer.motionX * theplayer.motionX + theplayer.motionZ * theplayer.motionZ);
			                	
			                	//System.out.println(curSpeed);
			                	
			                	double max = lastGroundSpeed;
			                	
			                	if (!dodged && curSpeed-0.1D > max) {
			                		theplayer.motionX = (theplayer.motionX / 0.91D) * (0.99D-((curSpeed-max) * 0.5D));
			                    	theplayer.motionZ = (theplayer.motionZ / 0.91D) * (0.99D-((curSpeed-max) * 0.5D));
			                	}
			                	
			                	//fix drag
			                	if (wasOnGround) {
			                		//System.out.println("fix drag");
			                		wasOnGround = false;
			                		if (lastGroundVel != null) {
			    	            		//theplayer.motionX = lastGroundVel.xCoord * 1.3D;
			    	            		//theplayer.motionY = lastGroundVel.yCoord;
			    	            		//theplayer.motionZ = lastGroundVel.zCoord * 1.3D;
			                		}
			                	}
		                	}
	                	}
	                	
	                }
                }
                
                //double speed2 = Math.min(Math.max(0.02D, Math.sqrt(lastGroundVel.xCoord * lastGroundVel.xCoord + lastGroundVel.zCoord * lastGroundVel.zCoord) * 0.2F), 0.1F);
                
                //speed2 = 0.02F;
                
                //System.out.println(speed2);
                
                if (lastGroundVel != null) {
                	//setPrivateValueBoth(EntityPlayer.class, theplayer, "ci", "speedInAir", (float)speed2/*0.09F*/);
                }
            	
                lastWorldTime = worldRef.getWorldInfo().getWorldTime();
            }

            if (coordToggle) {
                //displayMessage((new StringBuilder()).append("X: ").append((int)mc.renderViewEntity.o).append(" Y: ").append((int)mc.renderViewEntity.p).append(" Z: ").append((int)mc.renderViewEntity.q).toString());
                String coordmsg = mc.renderViewEntity.getEntityId() + ": " + (new StringBuilder()).append("X: ").append((int)mc.renderViewEntity.posX).append(" Y: ").append((int)mc.renderViewEntity.posY).append(" Z: ").append((int)mc.renderViewEntity.posZ).toString();
                String deathmsg = "";

                if (deathX != -1) {
                    deathmsg = new StringBuilder().append("Last Death, X: " + deathX + " Y: " + deathY + " Z: " + deathZ).toString();
                }

                mc.fontRenderer.drawStringWithShadow(coordmsg, 3, 25, 0xffffff);
                mc.fontRenderer.drawStringWithShadow(deathmsg, 3, 35, 0xffffff);
                mc.fontRenderer.drawStringWithShadow(infoMsg, 3, 45, 0xffffff);
            }
            
            if (MovePlusCfg.blockInfoToggle) {
                if (mc.objectMouseOver != null && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                	
                	Block id = mc.theWorld.getBlock(mc.objectMouseOver.blockX, mc.objectMouseOver.blockY, mc.objectMouseOver.blockZ);
                	int meta = mc.theWorld.getBlockMetadata(mc.objectMouseOver.blockX, mc.objectMouseOver.blockY, mc.objectMouseOver.blockZ);
                	
                	//if not vanilla
                	//if (id > 145) {
                		
                		Block block = id;
                		
                		if (block != null) {
                			String name = "";
                			
                			ItemStack is = (new ItemStack(id, 1, meta));
                			
                			if (is != null) {
                				if (is.getItem() != null) {
                					name = is.getDisplayName();
                				}
                				
                				mc.fontRenderer.drawStringWithShadow("Look: " + name + ", ID: " + id + ", Meta: " + meta, 3, 55, 0xffffff);
                			}
                		}
                		
                	//}
                }
            }

            for(int var4 = 0; var4 < keys.length; ++var4) {
                checkKey(var4);
            }
            
            //try to fix double jump happening from first jump sometimes, in 1.7.10, confirmed this fixed it
            if (theplayer.onGround) {
            	//System.out.println("set false");
            	secondPress[4] = false;
            }

            if(!theplayer.onGround || theplayer.isInWater()/* && false && theplayer.ridingEntity == null*/) {
                airControl(theplayer);
            }

            if (devFeatures && !theplayer.onGround && Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
                theplayer.motionY = 0.0F;
            }

            if (theplayer.motionY < -0.1F) {
            	//System.out.println("fall speed: " + theplayer.motionY);
            	//System.out.println(theplayer.fallDistance);
            }
            
            if (MovePlusCfg.fallDamageReduction) {
	            if ((theplayer.motionY < -0.5F && theplayer.motionY > -1.2F) && theplayer.ridingEntity == null && !theplayer.isInWater() && !theplayer.isInsideOfMaterial(Material.lava) && !theplayer.isInsideOfMaterial(Material.cactus) && !theplayer.isInsideOfMaterial(Material.water)) {
	                //this.displayMessage("hmm?");
	            	//System.out.println("fixing fall - " + theplayer.motionY);
	                //theplayer.fallDistance = 0.0F;
	
	                if (theplayer instanceof EntityClientPlayerMP) {
	                    //theplayer.onGround = true;
	                    ((EntityClientPlayerMP)theplayer).sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(theplayer.motionX, -999D, -999D, theplayer.motionZ, true));
	                }
	            }
            }
            
            
            

            
            
            
            //this.displayMessage(new StringBuilder().append(theplayer.fallDistance + " - " + theplayer.motionY).toString());
            //if (true) return;
            
            
            /*for (int i = 0; i < this.worldRef.playerEntities.size(); i++) {
            	EntityPlayer ent = (EntityPlayer)this.worldRef.playerEntities.get(i);

            	if(!ent.username.equals("Corosus")) {
            		//theplayer.attackTargetEntityWithCurrentItem(theplayer);
            		mc.playerController.attackEntity(theplayer, ent);
            	}
            }*/

            /*if (theplayer.distanceWalkedModified > 15F && !theplayer.onGround) {
             theplayer.distanceWalkedModified = 0F;
            }*/
            /*float stepdist = 0F;
            try {
             stepdist = (float)Float.valueOf(ModLoader.getPrivateValue(Entity.class, theplayer, "b").toString()).floatValue();
             ModLoader.setPrivateValue(Entity.class, theplayer, "nextStepDistance", (int)theplayer.distanceWalkedModified+1);

            } catch (Exception ex) {
             try {
            	 stepdist = (float)Float.valueOf(ModLoader.getPrivateValue(Entity.class, theplayer, "nextStepDistance").toString()).floatValue();
               	 ModLoader.setPrivateValue(Entity.class, theplayer, "nextStepDistance", (int)theplayer.distanceWalkedModified+1);
             } catch (Exception ex2) {

                }
            }*/

            //System.out.println(stepdist);

            if(theplayer.onGround) {
                speedJumped = false;
                doubleJumped = false;
                dodged = false;
                currentJumpCount = 0;

                if(!lastTickLanded) {
                    lastTickLanded = true;
                    landTime = tickCount;
                }
            } else {
                lastTickLanded = false;
            }

            prevTimeCount = timeCount;
            timeCount = worldRef.getWorldInfo().getWorldTime();

            if(timeCount > prevTimeCount) {
                tickCount += timeCount - prevTimeCount;
            }

            /*if(speedMining) {
                if(!mc.isMultiplayerWorld()) {
                    try {
                        Object var9 = ModLoader.getPrivateValue(PlayerControllerSP.class, (PlayerControllerSP)ModLoader.getMinecraftInstance().playerController, "f");
                        float var5 = Float.valueOf(var9.toString()).floatValue();

                        if(var5 > 0.01F) {
                            var5 += 0.05F;

                            if(var5 > 1.0F) {
                                var5 = 1.0F;
                            }

                            ModLoader.setPrivateValue(PlayerControllerSP.class, (PlayerControllerSP)ModLoader.getMinecraftInstance().playerController, "f", Float.valueOf(var5));
                        }

                        ModLoader.setPrivateValue(PlayerControllerSP.class, (PlayerControllerSP)ModLoader.getMinecraftInstance().playerController, "i", Integer.valueOf(0));
                    } catch (Exception var7) {
                        ;
                    }
                } else if(!superMagicProperty) {
                    ;
                }
            }*/
        }

        if(timeout > 0 && msg != null) {
            ScaledResolution var8 = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int var4 = var8.getScaledWidth();
            int var10 = var8.getScaledHeight();
            int var6 = mc.fontRenderer.getStringWidth(msg);
            mc.fontRenderer.drawStringWithShadow(msg, 3, 105, 16777215);
            --timeout;
        }
		
	}
	
}

package dev.lvstrng.argon.module.modules.combat;

import dev.lvstrng.argon.event.events.HudListener;
import dev.lvstrng.argon.event.events.PacketSendListener;
import dev.lvstrng.argon.module.Category;
import dev.lvstrng.argon.module.Module;
import dev.lvstrng.argon.module.setting.BooleanSetting;
import dev.lvstrng.argon.module.setting.MinMaxSetting;
import dev.lvstrng.argon.module.setting.NumberSetting;
import dev.lvstrng.argon.utils.EncryptedString;
import dev.lvstrng.argon.utils.TimerUtils;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public final class AutoWTap extends Module implements PacketSendListener, HudListener {
	private final MinMaxSetting delay = new MinMaxSetting(EncryptedString.of("Delay"), 0, 1000, 1,230, 270);
	private final BooleanSetting inAir = new BooleanSetting(EncryptedString.of("In Air"), false)
			.setDescription(EncryptedString.of("Whether it should W tap in air"));
	private final TimerUtils sprintTimer = new TimerUtils();
	private final TimerUtils tapTimer = new TimerUtils();

	private boolean holdingForward;
	private boolean sprinting;
	private int currentDelay;

	public AutoWTap() {
		super(EncryptedString.of("Auto WTap"),
				EncryptedString.of("Automatically W Taps for you so the opponent takes more knockback"),
				-1,
				Category.COMBAT);
		addSettings(delay, inAir);
	}

	@Override
	public void onEnable() {
		eventManager.add(PacketSendListener.class, this);
		eventManager.add(HudListener.class, this);
		currentDelay = delay.getRandomValueInt();
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(PacketSendListener.class, this);
		eventManager.remove(HudListener.class, this);
		super.onDisable();
	}

	@Override
	public void onRenderHud(HudEvent event) {
		if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_W) != 1) {
			sprinting = false;
			holdingForward = false;
			return;
		}

		if (!inAir.getValue() && !mc.player.isOnGround())
			return;

		if (holdingForward && tapTimer.delay(1)) {
			mc.options.forwardKey.setPressed(false);
			sprintTimer.reset();
			sprinting = true;
			holdingForward = false;
		}

		if (!sprinting || !sprintTimer.delay(currentDelay))
			return;

		mc.options.forwardKey.setPressed(true);
		sprinting = false;
		currentDelay = delay.getRandomValueInt();
	}

	@Override
	public void onPacketSend(PacketSendEvent event) {
		if (!(event.packet instanceof PlayerInteractEntityC2SPacket packet))
			return;

		packet.handle(new PlayerInteractEntityC2SPacket.Handler() {
			@Override
			public void interact(Hand hand) {
			}

			@Override
			public void interactAt(Hand hand, Vec3d pos) {
			}

			@Override
			public void attack() {
				if (mc.options.forwardKey.isPressed() && mc.player.isSprinting()) {
					sprintTimer.reset();
					holdingForward = true;
				}
			}
		});
	}
}
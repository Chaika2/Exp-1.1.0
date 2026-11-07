package aethereal.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class WavSoundPlayer {
    public static final WavSoundPlayer INSTANCE = new WavSoundPlayer();
    public Clip currentClip;

    public synchronized void playSound(String str, float f, boolean z) {
        new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(((Resource) MinecraftClient.getInstance().getResourceManager().getResource(Identifier.of("expensive", "sounds/" + str + ".wav")).orElseThrow(() -> {
                    return new IOException("Sound resource not found: " + str);
                })).getInputStream());
                try {
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
                    try {
                        clip.open(audioInputStream);
                        setVolume(clip, f);
                        clip.start();
                        if (z) {
                            clip.loop(-1);
                        }
                        synchronized (this) {
                            try {
                                this.currentClip = clip;
                            } catch (Throwable th) {
                                throw th;
                            }
                        }
                        if (audioInputStream != null) {
                            audioInputStream.close();
                        }
                        bufferedInputStream.close();
                    } catch (Throwable th2) {
                        if (audioInputStream != null) {
                            try {
                                audioInputStream.close();
                            } catch (Throwable th3) {
                                th2.addSuppressed(th3);
                            }
                        }
                        throw th2;
                    }
                } catch (Throwable th4) {
                    try {
                        bufferedInputStream.close();
                    } catch (Throwable th5) {
                        th4.addSuppressed(th5);
                    }
                    throw th4;
                }
            } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                ChatUtil.addChatMessage("музика наебнулась: " + e.getMessage());
            }
        }).start();
    }

    public synchronized void playSoundSequentially(List<String> list, float f, long j) {
        new Thread(() -> {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                try {
                    Clip clip = AudioSystem.getClip();
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(((Resource) MinecraftClient.getInstance().getResourceManager().getResource(Identifier.of("expensive", "sounds/" + str + ".wav")).orElseThrow(() -> {
                        return new IOException("Sound resource not found: " + str);
                    })).getInputStream());
                    try {
                        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
                        try {
                            clip.open(audioInputStream);
                            setVolume(clip, f);
                            clip.start();
                            Thread.sleep(j);
                            clip.close();
                            if (audioInputStream != null) {
                                audioInputStream.close();
                            }
                            bufferedInputStream.close();
                        } catch (Throwable th) {
                            if (audioInputStream != null) {
                                try {
                                    audioInputStream.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        try {
                            bufferedInputStream.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                        throw th3;
                    }
                } catch (UnsupportedAudioFileException | LineUnavailableException | IOException | InterruptedException e) {
                    ChatUtil.addChatMessage("Ошибка воспроизведения звука: " + str + " - " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public synchronized void stopSound() {
        if (this.currentClip != null) {
            this.currentClip.stop();
            this.currentClip.close();
            this.currentClip = null;
        }
    }

    public void setVolume(Clip clip, float f) {
        FloatControl control = (FloatControl) (clip.getControl(FloatControl.Type.MASTER_GAIN));
        float minimum = control.getMinimum();
        control.setValue(minimum + ((control.getMaximum() - minimum) * (f / 100.0f)));
    }
}

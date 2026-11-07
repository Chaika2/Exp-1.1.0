using System;
using System.Drawing;
using System.IO;
using System.Text;
using System.Threading;
using System.Windows.Forms;
using System.Diagnostics;

namespace ExpensiveLauncher
{
    public class MainForm : Form
    {
        private TextBox nickBox;
        private TrackBar ramBar;
        private Label ramLabel;
        private TextBox dirBox;
        private Button browseBtn;
        private Button runBtn;
        private Button stopBtn;
        private TextBox logBox;
        private Process proc;
        private string configPath;
        private volatile bool running;

        public MainForm()
        {
            Init();
            configPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "launcher_config.ini");
            LoadConfig();
        }

        private void Init()
        {
            Text = "Expensive Launcher";
            ClientSize = new Size(470, 430);
            FormBorderStyle = FormBorderStyle.FixedSingle;
            MaximizeBox = false;
            StartPosition = FormStartPosition.CenterScreen;
            Font = new Font("Segoe UI", 9F);
            AutoScaleMode = AutoScaleMode.Font;

            int x = 140, y = 16;

            Controls.Add(new Label { Text = "Ник:", Location = new Point(16, y + 3), AutoSize = true });
            nickBox = new TextBox { Location = new Point(x, y), Width = 315 };
            Controls.Add(nickBox);

            y += 34;
            Controls.Add(new Label { Text = "RAM (ГБ):", Location = new Point(16, y + 9), AutoSize = true });
            ramBar = new TrackBar { Location = new Point(x, y - 4), Width = 250, Minimum = 1, Maximum = 16, Value = 4, TickStyle = TickStyle.None };
            ramLabel = new Label { Text = "4 ГБ", Location = new Point(x + 258, y + 8), Width = 60, AutoSize = false };
            ramBar.ValueChanged += delegate { ramLabel.Text = ramBar.Value + " ГБ"; };
            Controls.Add(ramBar);
            Controls.Add(ramLabel);

            y += 36;
            Controls.Add(new Label { Text = "Исходники:", Location = new Point(16, y + 3), AutoSize = true });
            dirBox = new TextBox { Location = new Point(x, y), Width = 235 };
            browseBtn = new Button { Text = "Обзор...", Location = new Point(x + 243, y - 1), Width = 78 };
            browseBtn.Click += delegate { BrowseFolder(); };
            Controls.Add(dirBox);
            Controls.Add(browseBtn);

            y += 40;
            runBtn = new Button { Text = "Запуск", Location = new Point(x, y), Size = new Size(130, 28), BackColor = Color.FromArgb(120, 180, 120) };
            stopBtn = new Button { Text = "Стоп", Location = new Point(x + 138, y), Size = new Size(90, 28), Enabled = false, BackColor = Color.FromArgb(200, 120, 120) };
            Controls.Add(runBtn);
            Controls.Add(stopBtn);
            y += 42;

            logBox = new TextBox { Location = new Point(16, y), Width = ClientSize.Width - 32, Height = ClientSize.Height - y - 16, Multiline = true, ReadOnly = true, ScrollBars = ScrollBars.Both, BackColor = Color.FromArgb(20, 20, 20), ForeColor = Color.FromArgb(220, 220, 220) };
            Controls.Add(logBox);

            runBtn.Click += delegate { Launch(); };
            stopBtn.Click += delegate { StopProcess(); };
            FormClosing += delegate(object s, FormClosingEventArgs e) { OnClosing(e); };
        }

        private void BrowseFolder()
        {
            using (FolderBrowserDialog dlg = new FolderBrowserDialog())
            {
                dlg.Description = "Выбери папку с исходниками клиента";
                if (dirBox.Text.Trim().Length > 0 && Directory.Exists(dirBox.Text.Trim()))
                {
                    dlg.SelectedPath = dirBox.Text.Trim();
                }
                if (dlg.ShowDialog(this) == DialogResult.OK)
                {
                    dirBox.Text = dlg.SelectedPath;
                }
            }
        }

        private string DefaultSourceDir()
        {
            string baseDir = AppDomain.CurrentDomain.BaseDirectory;
            try
            {
                if (File.Exists(Path.Combine(baseDir, "build.gradle")))
                {
                    return baseDir;
                }
                string parent = Path.GetFullPath(Path.Combine(baseDir, ".."));
                if (File.Exists(Path.Combine(parent, "build.gradle")))
                {
                    return parent;
                }
            }
            catch (Exception) { }
            return baseDir;
        }

        private void LoadConfig()
        {
            string nick = "";
            string dir = "";
            int ram = 4;
            if (File.Exists(configPath))
            {
                foreach (string line in File.ReadAllLines(configPath, Encoding.UTF8))
                {
                    string s = line.Trim();
                    if (s.Length == 0 || s.StartsWith("#")) continue;
                    int eq = s.IndexOf('=');
                    if (eq < 0) continue;
                    string key = s.Substring(0, eq).Trim();
                    string val = s.Substring(eq + 1).Trim();
                    if (key == "nick") nick = val;
                    else if (key == "dir") dir = val;
                    else if (key == "ram")
                    {
                        int v;
                        if (int.TryParse(val, out v) && v >= 1 && v <= 16) ram = v;
                    }
                }
            }
            nickBox.Text = nick;
            dirBox.Text = dir.Length > 0 ? dir : DefaultSourceDir();
            ramBar.Value = ram;
        }

        private void SaveConfig()
        {
            try
            {
                StringBuilder sb = new StringBuilder();
                sb.AppendLine("# Expensive Launcher config");
                sb.AppendLine("nick=" + nickBox.Text.Trim());
                sb.AppendLine("dir=" + dirBox.Text.Trim());
                sb.AppendLine("ram=" + ramBar.Value);
                File.WriteAllText(configPath, sb.ToString(), Encoding.UTF8);
            }
            catch (Exception) { }
        }

        private void AppendLog(string line)
        {
            if (logBox.InvokeRequired)
            {
                logBox.Invoke((MethodInvoker)delegate { AppendLog(line); });
                return;
            }
            logBox.AppendText(line.Replace("\r", "") + Environment.NewLine);
            logBox.SelectionStart = logBox.Text.Length;
            logBox.ScrollToCaret();
        }

        private void UIThread(MethodInvoker action)
        {
            if (logBox.InvokeRequired)
            {
                logBox.Invoke(action);
                return;
            }
            action();
        }

        private void SetRunning(bool value)
        {
            UIThread(delegate
            {
                running = value;
                runBtn.Enabled = !value;
                stopBtn.Enabled = value;
            });
        }

        private static string JsonEscape(string s)
        {
            StringBuilder sb = new StringBuilder();
            foreach (char c in s)
            {
                switch (c)
                {
                    case '"': sb.Append("\\\""); break;
                    case '\\': sb.Append("\\\\"); break;
                    case '\b': sb.Append("\\b"); break;
                    case '\f': sb.Append("\\f"); break;
                    case '\n': sb.Append("\\n"); break;
                    case '\r': sb.Append("\\r"); break;
                    case '\t': sb.Append("\\t"); break;
                    default:
                        if (c < 0x20) sb.Append("\\u" + ((int)c).ToString("x4"));
                        else sb.Append(c);
                        break;
                }
            }
            return sb.ToString();
        }

        private void WriteNickname(string srcDir, string nick)
        {
            try
            {
                string cfgDir = Path.Combine(srcDir, "run", "expensive", "config");
                Directory.CreateDirectory(cfgDir);
                string json = "{\n  \"nickname\": \"" + JsonEscape(nick) + "\"\n}";
                File.WriteAllText(Path.Combine(cfgDir, "session-nickname.json"), json, new UTF8Encoding(false));
                AppendLog("[Launcher] Ник \"" + nick + "\" сохранён в session-nickname.json");
            }
            catch (Exception ex)
            {
                AppendLog("[Launcher] Ошибка записи ника: " + ex.Message);
            }
        }

        private void Launch()
        {
            if (running) return;

            string srcDir = dirBox.Text.Trim();
            try
            {
                srcDir = Path.GetFullPath(srcDir);
            }
            catch (Exception)
            {
                MessageBox.Show(this, "Путь к исходникам некорректен.", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }

            if (!File.Exists(Path.Combine(srcDir, "build.gradle")) || !File.Exists(Path.Combine(srcDir, "gradlew.bat")))
            {
                MessageBox.Show(this, "В выбранной папке не найдено build.gradle / gradlew.bat.\nУкажи папку с исходниками проекта.", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }

            string nick = nickBox.Text.Trim();
            if (nick.Length == 0)
            {
                MessageBox.Show(this, "Введи ник.", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }

            SaveConfig();
            WriteNickname(srcDir, nick);

            int ram = ramBar.Value;
            string jvmOpts = "-Xmx" + ram + "G -Xms512M";

            try
            {
                ProcessStartInfo psi = new ProcessStartInfo();
                psi.FileName = Path.Combine(srcDir, "gradlew.bat");
                psi.Arguments = "runClient --console=plain";
                psi.WorkingDirectory = srcDir;
                psi.UseShellExecute = false;
                psi.CreateNoWindow = true;
                psi.RedirectStandardOutput = true;
                psi.RedirectStandardError = true;
                psi.EnvironmentVariables["JAVA_TOOL_OPTIONS"] = jvmOpts;

                proc = new Process();
                proc.StartInfo = psi;
                proc.Start();

                AppendLog("----------------------------------------");
                AppendLog("[Launcher] Запуск клиента...  RAM: " + ram + " ГБ  JVM: " + jvmOpts);
                AppendLog("[Launcher] Первый запуск может быть медленным (сборка через Gradle).");
                SetRunning(true);

                Thread tOut = new Thread(new ThreadStart(delegate { Pump(proc.StandardOutput); }));
                Thread tErr = new Thread(new ThreadStart(delegate { Pump(proc.StandardError); }));
                Thread tWait = new Thread(new ThreadStart(delegate
                {
                    try { proc.WaitForExit(); } catch (Exception) { }
                    proc = null;
                    SetRunning(false);
                    AppendLog("[Launcher] Процесс завершён.");
                }));
                tOut.IsBackground = true;
                tErr.IsBackground = true;
                tWait.IsBackground = true;
                tOut.Start();
                tErr.Start();
                tWait.Start();
            }
            catch (Exception ex)
            {
                AppendLog("[Launcher] Ошибка запуска: " + ex.Message);
                proc = null;
                SetRunning(false);
            }
        }

        private void Pump(StreamReader reader)
        {
            try
            {
                char[] buf = new char[1024];
                StringBuilder line = new StringBuilder();
                int n;
                while ((n = reader.Read(buf, 0, buf.Length)) > 0)
                {
                    for (int i = 0; i < n; i++)
                    {
                        char c = buf[i];
                        if (c == '\n')
                        {
                            AppendLog(line.ToString());
                            line.Length = 0;
                        }
                        else if (c != '\r')
                        {
                            line.Append(c);
                        }
                    }
                }
                if (line.Length > 0) AppendLog(line.ToString());
            }
            catch (Exception) { }
        }

        private void StopProcess()
        {
            if (proc != null)
            {
                try
                {
                    int pid = proc.Id;
                    ProcessStartInfo psi = new ProcessStartInfo("taskkill", "/F /T /PID " + pid);
                    psi.UseShellExecute = false;
                    psi.CreateNoWindow = true;
                    psi.WindowStyle = ProcessWindowStyle.Hidden;
                    using (Process killer = Process.Start(psi))
                    {
                        killer.WaitForExit(5000);
                    }
                    AppendLog("[Launcher] Процесс остановлен.");
                }
                catch (Exception ex)
                {
                    AppendLog("[Launcher] Ошибка остановки: " + ex.Message);
                }
            }
        }

        private void OnClosing(FormClosingEventArgs e)
        {
            if (!running) return;
            if (MessageBox.Show(this, "Игра ещё запущена. Остановить процесс и выйти?", "Выход", MessageBoxButtons.YesNo, MessageBoxIcon.Question) == DialogResult.Yes)
            {
                StopProcess();
            }
            else
            {
                e.Cancel = true;
            }
        }
    }

    internal static class Program
    {
        [STAThread]
        private static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MainForm());
        }
    }
}
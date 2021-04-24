using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.IO;
using System.Text.RegularExpressions;
using System.Xml.Linq;
using ClipboardManager.models;
using CSScriptLibrary;
using PropertyChanged;

namespace Components
{
    public static class Interpreter
    {
        public const string SCRIPT_EXTENSION = ".xcs";
        
        #region Runner

        /// <summary>
        /// A state representing success or failure of running interpreter.
        /// </summary>
        public abstract class Result
        {
            public class Success : Result
            {
                public bool ShouldProceed { get; private set; }
                public Success(bool shouldProceed)
                {
                    ShouldProceed = shouldProceed;
                }
            }
            public class Error : Result
            {
                public string Message { get; private set; }
                public Error(string message)
                {
                    Message = message;
                }
            }

            /// <summary>
            /// Determines that should we proceed with the next call.
            /// </summary>
            /// <returns>"True" to abort &amp; "False" to proceed.</returns>
            public bool ShouldExecute()
            {
                if (this is Success)
                {
                    var s = (Success) this;
                    return s.ShouldProceed;
                }
                return false;
            }
        }

        /// <summary>
        /// Runs the script.
        /// </summary>
        /// <returns></returns>
        public static Result Run(Script script, Clipper clip)
        {
            try
            {
                dynamic runner = CSScript.LoadCode(script.Code).CreateObject("*");
                if (runner == null)
                    return new Result.Error("Error: No object/class found in the code.");

                dynamic returned = runner.Run(clip);
                if (returned is bool)
                {
                    var result = (bool) returned;
                    return new Result.Success(result);
                }
                else return new Result.Error("Error: The method must return true/false.");
            }
            catch (Exception e)
            {
                var message = Regex.Replace(e.Message, @"(c|C)\:\\Users\\.*\\dynamic\\([\d\.a-z-]+)", "Script");
                return new Result.Error($"Error: {message}");
            }
            
            return new Result.Error("Error: Couldn't determine the failure of execution.");
        }

        #endregion
        
        #region Properties
        public static ObservableCollection<Script> OnCopyScripts { get; private set; } = new();
        public static ObservableCollection<Script> OnPasteScripts { get; private set; } = new();

        #endregion

        #region Read/Write
        
        /// <summary>
        /// Load the scripts from the <see cref="Constants.ApplicationScriptsDirectory"/>
        /// </summary>
        public static void LoadScripts()
        {
            Init();
            var copyScripts = new List<Script>();
            var pasteScripts = new List<Script>();
            
            InternalLoad(copyScripts, Constants.CopyScriptsPath);
            InternalLoad(pasteScripts, Constants.PasteScriptsPath);

            OnCopyScripts = new(copyScripts);
            OnPasteScripts = new(pasteScripts);

            SubscribeChanges();
        }

        /// <summary>
        /// Write scripts to the <see cref="Constants.ApplicationScriptsDirectory"/>
        /// </summary>
        public static void WriteScripts()
        {
            Init();
            InternalWrite(OnCopyScripts, Constants.CopyScriptsPath);
            InternalWrite(OnPasteScripts, Constants.PasteScriptsPath);
        }

        private static void InternalWrite(IEnumerable<Script> list, string directory)
        {
            foreach (var f in new DirectoryInfo(directory).GetFiles($"*{SCRIPT_EXTENSION}")) f.Delete();
            foreach (Script script in list)
            {
                var doc = new XDocument();
                doc.Add(script.ToNode());
                doc.Save($"{directory}\\{script.Name}{SCRIPT_EXTENSION}");
            }
        }
        
        private static void InternalLoad(List<Script> list, string directory)
        {
            foreach (string file in Directory.GetFiles(directory, $"*{SCRIPT_EXTENSION}"))
            {
                try
                {
                    var ele = XDocument.Load(file).Element(nameof(Script));
                    list.Add(Script.FromNode(ele));
                }
                catch (Exception e)
                {
                    LogHelper.Log(nameof(Interpreter), "Couldn't parse script: " + file + "\n" + e.StackTrace);
                }
            }
        }

        private static void SubscribeChanges()
        {
            OnCopyScripts.CollectionChanged += (o, e) => InternalWrite(OnCopyScripts, Constants.CopyScriptsPath);
            OnPasteScripts.CollectionChanged += (o, e) => InternalWrite(OnPasteScripts, Constants.PasteScriptsPath);
        }
        
        private static void Init()
        {
            if (!Directory.Exists(Constants.CopyScriptsPath)) Directory.CreateDirectory(Constants.CopyScriptsPath);
            if (!Directory.Exists(Constants.PasteScriptsPath)) Directory.CreateDirectory(Constants.PasteScriptsPath);
        }
        
        #endregion
    }

    public class Clipper
    {
        public string RawText { get; set; }
        public string ImagePath { get; private set; }
        public ContentType Type { get; private set; }

        public static Clipper CreateSandbox() => new()
        {
            RawText = "This is a sample data",
            ImagePath = null,
            Type = ContentType.Text
        };
    }

    [ImplementPropertyChanged]
    public class Script : INotifyPropertyChanged, IEquatable<Script>
    {
        public const string BASE_TEMPLATE = @"using System;
using System.Windows.Forms;
using Components;

public class MyScript {
    public bool Run(Clipper clip) {
        MessageBox.Show(clip.RawText);
        return false;
    }
}
";
        public string Name { get; set; } = string.Empty;
        public string Code { get; set; } = BASE_TEMPLATE;
        public bool Enabled { get; set; } = true;

        public event PropertyChangedEventHandler PropertyChanged;

        public XElement ToNode()
        {
            var element = new XElement(nameof(Script));
            element.Add(new XElement(nameof(Name), Name));
            element.Add(new XElement(nameof(Enabled), Enabled));
            element.Add(new XElement(nameof(Code), Code));
            return element;
        }

        public static Script FromNode(XElement t)
        {
            var script = new Script();
            script.Name = t.Element(nameof(Name)).Value;
            script.Enabled = t.Element(nameof(Enabled)).Value.ToBool();
            script.Code = t.Element(nameof(Code)).Value;
            return script;
        }

        public override bool Equals(object obj) => Equals(obj as Script);
        public bool Equals(Script other) => other != null && Name == other.Name && Code == other.Code && Enabled == other.Enabled;

        public override int GetHashCode()
        {
            int hashCode = 2094519000;
            hashCode = hashCode * -1521134295 + EqualityComparer<string>.Default.GetHashCode(Name);
            hashCode = hashCode * -1521134295 + EqualityComparer<string>.Default.GetHashCode(Code);
            hashCode = hashCode * -1521134295 + Enabled.GetHashCode();
            return hashCode;
        }
    }
}
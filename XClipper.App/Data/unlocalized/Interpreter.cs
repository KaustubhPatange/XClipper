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
using XClipper;

namespace Components
{
    // Copy script interpreter is hooked at TableHelper.cs
    // Paste script interpreter is hooked at ClipboardHelper.cs
    
    /// <summary>
    /// A small interpreter to transform data using CSScript on copy or paste action.
    /// </summary>
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
            /// Determines the abort status.
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
                if (!Regex.IsMatch(script.Code, @"public\s+bool\s+([A-Za-z0-9_]+)(\s+)?\((\s+)?Clipper\s+([A-Za-z0-9_]+)\)"))
                    return new Result.Error("Error: No \"public\" method found accepting \"Clipper\" as parameter.\nHint: Add \"public bool Run(Clipper clip) { return false; }\"");
                
                var runner = CSScript.CreateFunc<bool>(script.Code);
                if (runner == null)
                    return new Result.Error("Error: No object/class found in the code.");

                if (Regex.IsMatch(script.Code, @"using(\s+)Components(\s+)?;"))
                    return new Result.Error("Error: Using Components.dll assembly is prohibited.");

                var result = runner.Invoke(clip);
                return new Result.Success(result);
            }
            catch (Exception e)
            {
                var message = Regex.Replace(e.Message, @"(c|C)\:\\Users\\.*\\dynamic\\([\d\.a-z-]+)", "Script");
                return new Result.Error($"Error: {message}");
            }
            
            return new Result.Error("Error: Couldn't determine the failure of execution.");
        }
        
        /// <summary>
        /// <inheritdoc cref="InternalBatchRunScript"/>
        /// </summary>
        /// <returns><inheritdoc cref="InternalBatchRunScript"/></returns>
        public static bool BatchRunCopyScripts(Clipper clip) => InternalBatchRunScript(OnCopyScripts, clip);
        
        /// <summary>
        /// <inheritdoc cref="InternalBatchRunScript"/>
        /// </summary>
        /// <returns><inheritdoc cref="InternalBatchRunScript"/></returns>
        public static bool BatchRunPasteScripts(Clipper clip) => InternalBatchRunScript(OnPasteScripts, clip);

        /// <summary>
        /// Batch run copy scripts &amp; returns the abort status.
        /// </summary>
        /// <returns>"True" to abort &amp; "False" to continue.</returns>
        private static bool InternalBatchRunScript(ObservableCollection<Script> list, Clipper clip)
        {
            if (list.Count > 0)
            {
                foreach (var script in list)
                {
                    if (script.Enabled)
                    {
                        bool abort = Run(script, clip).ShouldExecute();
                        if (abort) return true;
                    }
                }
            }
            return false;
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

    [ImplementPropertyChanged]
    public class Script : INotifyPropertyChanged, IEquatable<Script>
    {
        public const string BASE_TEMPLATE = @"using System;
using XClipper;

public bool Run(Clipper clip) {
    clip.RawText = clip.RawText.Trim();
    return false;
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
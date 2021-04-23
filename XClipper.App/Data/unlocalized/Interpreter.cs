using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Text.RegularExpressions;
using System.Xml.Linq;
using PropertyChanged;

namespace Components
{
    public static class Interpreter
    {
        public const string SCRIPT_EXTENSION = ".xcs";
        
        #region Properties
        
        private static List<Script> _CopyScripts { get; set; } = new();
        private static List<Script> _PasteScripts { get; set; } = new();

        public static List<Script> OnCopyScripts
        {
            get => _CopyScripts;
            set
            {
                if (value != _CopyScripts)
                {
                    _CopyScripts = value;
                    DefaultSettings.NotifyStaticPropertyChanged(nameof(OnCopyScripts));
                }
            }
        }
        public static List<Script> OnPasteScripts
        {
            get => _PasteScripts;
            set
            {
                if (value != _PasteScripts)
                {
                    _PasteScripts = value;
                    DefaultSettings.NotifyStaticPropertyChanged(nameof(OnPasteScripts));
                }
            }
        }
        
        #endregion

        #region Script Update

        public static void UpdateCopyScript(List<Script> list)
        {
            OnCopyScripts = list;
            InternalWrite(list, Constants.CopyScriptsPath);
        }
        
        public static void UpdatePasteScript(List<Script> list)
        {
            OnPasteScripts = list;
            InternalWrite(list, Constants.PasteScriptsPath);
        }

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
            
            OnCopyScripts = copyScripts;
            OnPasteScripts = pasteScripts;
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

        private static void InternalWrite(List<Script> list, string directory)
        {
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
        
        private static void Init()
        {
            if (!Directory.Exists(Constants.CopyScriptsPath)) Directory.CreateDirectory(Constants.CopyScriptsPath);
            if (!Directory.Exists(Constants.PasteScriptsPath)) Directory.CreateDirectory(Constants.PasteScriptsPath);
        }
        
        #endregion
    }

    public class Script : INotifyPropertyChanged, IEquatable<Script>
    {
        public string Name { get; set; }
        public string Code { get; set; }
        public bool Enabled { get; set; }

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
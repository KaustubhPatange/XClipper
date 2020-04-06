using ClipboardManager.models;
using SQLite;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace WpfTesting.viewModels
{
    public class ClipWindowViewModel
    {
        private ClipBinder binder;

        private static string baseDirectory = AppDomain.CurrentDomain.BaseDirectory;
        private string databasePath;

        private SQLiteConnection dataDB;

        public ClipWindowViewModel setBinder(ClipBinder binder)
        {
            this.binder = binder;
            databasePath = Path.Combine(baseDirectory, "data.db");
            dataDB = new SQLiteConnection(databasePath);
            return this;
        }

        public List<TableCopy> ClipData
        {
            get { return dataDB.Query<TableCopy>("select * from TableCopy"); }
        }
    }
}

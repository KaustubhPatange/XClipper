using System;
using System.Data.SqlClient;
using System.IO;
using System.Linq;
using ClipboardManager.models;
using Components;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using SQLite;
using static Components.Core;

namespace XClipper.Tests
{
    [TestClass]
    public class SQLTest
    {
        private string DatabasePath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "data.db");

        [TestMethod]
        public void SQLPasswordTest()
        {
            
            SQLiteConnectionString options = new SQLiteConnectionString(DatabasePath, true, CONNECTION_PASS);

            SQLiteConnection con = new SQLiteConnection(options);

            con.CreateTable<TableCopy>();

            var t = new TableCopy
            {
                Text = "Demo",
                LongText = "dnejdedjkjd",
                ContentType = ContentType.Text,
                IsPinned = false,
                DateTime = "ddkdd",
                RawText = "yyyy"
            };
            var t1 = new TableCopy
            {
                Text = "Demo1",
                LongText = "dnejdeddwdawdjkjd",
                ContentType = ContentType.Image,
                IsPinned = true,
                DateTime = "dddwswkdd",
                RawText = "mdnddj"
            };

            con.Insert(t);
            con.Insert(t1);
        }
        [TestMethod]
        public void SQLRetrieveTest()
        {
            SQLiteConnectionString options = new SQLiteConnectionString(DatabasePath, true, CONNECTION_PASS);
            SQLiteConnection con = new SQLiteConnection(options);

            var s = con.Query<TableCopy>("select * from TableCopy").ToList();

            Assert.AreEqual(4, s.Count);
        }
    }
}

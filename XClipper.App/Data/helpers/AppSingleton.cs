using ClipboardManager.models;
using System.Collections.Generic;
using System.Linq;
using static Components.MainHelper;
using static Components.DefaultSettings;
using static Components.TableHelper;
using System.Threading.Tasks;
using Autofac;
using static WK.Libraries.SharpClipboardNS.SharpClipboard;
using System;
using System.Diagnostics;
using System.Drawing.Printing;

#nullable enable

namespace Components.viewModels
{
    public class AppSingleton
    {
        #region Variable Declaration

        private int TotalClips = 0;
        private int TotalPage = 0;
        private int Page = 1;

        private IClipBinder Binder;
        private IDatabase<TableCopy> dataDB;
       // public SQLiteConnection dataDB;
        private static AppSingleton Instance = null;

        #endregion


        #region Singleton

        public static AppSingleton GetInstance
        {
            get
            {
                if (Instance == null)
                    Instance = new AppSingleton();
                return Instance;
            }
        }

        private AppSingleton()
        {
            dataDB = AppModule.Container.Resolve<IDatabase<TableCopy>>();
        }

        #endregion


        #region Methods

        public void ClearPaging() => Page = 1;
        public bool CanFetchNext()
        {
            if (Page < TotalPage)
            {
                Page++;
                return true;
            }
            return false;
        }
        public void Close() => dataDB.CloseConnection();
        public void Init() => dataDB.Initialize();

        public void SetBinder(IClipBinder Binder)
        {
            this.Binder = Binder;
        }


        #endregion


        #region IClipBinder Invokes

        /// <summary>
        /// This will make an exit request to main window to close the window.
        /// </summary>
        public void MakeExitRequest()
        {
            Binder.OnExitRequest();
        }
        public void DeleteData(TableCopy model)
        {
            DeleteClipData(model);
            Binder.OnModelDeleted(ClipData);
        }
        public void DeleteData(List<TableCopy> models)
        {
            models.ForEach((model) => { DeleteClipData(model); });
            Binder.OnModelDeleted(ClipData);
        }
        public void TogglePin(TableCopy model)
        {
            model.IsPinned = !model.IsPinned;
            dataDB.Query("update TableCopy set IsPinned = ? where Id = ?", model.IsPinned, model.Id);
            Binder.OnModelDeleted(ClipData);
        }
        public void UpdateData(TableCopy model)
        {
            // We will encrypt data here coz... why not...
            dataDB.Query("update TableCopy set Text = ?, LongText = ?, RawText = ? where Id = ?", model.Text, model.LongText, model.RawText, model.Id);
            Binder.OnPopupTextEdited(ClipData);
        }

        public void UpdateLastUsedTime(TableCopy model)
        {
            dataDB.Query("update TableCopy set LastUsedDateTime = ? where Id = ?", model.LastUsedDateTime, model.Id);
        }

        #endregion


        #region Data Filtering

        public void SetFilterText(string Text) => Binder.OnFilterTextEdit(Text);

        public List<TableCopy> FilterTextLengthDesc()
        {
            var data = dataDB.GetAllData();
            return data.Where(x => x.ContentType == ContentType.Text).OrderByDescending(x => x.RawText.Length).Take(TruncateList).ToList();
        }

        public List<TableCopy> FilterTextLengthAsc()
        {
            var data = dataDB.GetAllData();
            return data.Where(x => x.ContentType == ContentType.Text).OrderBy(x => x.RawText.Length).Take(TruncateList).ToList();
        }
        public List<TableCopy> FilterOldest() => dataDB.GetAllData().Take(TruncateList).ToList();
        public List<TableCopy> FilterNewest() => dataDB.GetAllData().Reverse<TableCopy>().Take(TruncateList).ToList();
        public List<TableCopy> FilterData(string text) => dataDB.GetAllData().Where(s => s.Text.ToLower().Contains(text.ToLower())).Reverse().Take(TruncateList).ToList();
        public List<TableCopy> FilterContentType(ContentType type) => dataDB.GetAllData().Where(s => s.ContentType == type).Reverse().Take(TruncateList).ToList();
        public List<TableCopy> FilterPinned() => dataDB.GetAllData().Where(s => s.IsPinned).Reverse().Take(TruncateList).ToList();
        public List<TableCopy> FilterUnpinned() => dataDB.GetAllData().Where(s => !s.IsPinned).Reverse().Take(TruncateList).ToList();

        #endregion


        #region Data Obtaining Methods

        public List<TableCopy> GetAllData() => dataDB.GetAllData();

        public List<TableCopy> ClipData
        {
            get
            {
                var pinnedItems = dataDB.GetAllData().Where(x => x.IsPinned).Reverse();
                var normalItems = dataDB.GetAllData().Where(x => !x.IsPinned)
                    .OrderByDescending(x => ParseDateTimeText(x.LastUsedDateTime));

                TotalClips = pinnedItems.Count() + normalItems.Count();
                TotalPage = (TotalClips / TruncateList) + ((TotalClips % TruncateList != 0) ? 1 : 0);

                // todo: Disabling pagination
               // return pinnedItems.Concat(normalItems).Take(TruncateList * Page).ToList();
                return pinnedItems.Concat(normalItems).ToList();
            }
        }

        #endregion


        #region SQLite Methods

        #region Insert Content

        public void InsertAll(List<TableCopy> models) => dataDB.InsertAll(models);

        public void InsertContent(TableCopy model, bool pushToDatabase = true)
        {
            // This will check if same clip text is saved again
            var list = dataDB.GetAllData().OrderByDescending(s => ParseDateTimeText(s.LastUsedDateTime)).ToList();
            foreach (var c in list)
            {
                if (c.ContentType == model.ContentType)
                {
                    switch (model.ContentType)
                    {
                        case ContentType.Text:
                            if (model.Text == c.Text) return;
                            break;
                        case ContentType.Image:
                            if (model.ImagePath == c.ImagePath) return;
                            break;
                        case ContentType.Files:
                            if (model.LongText == c.LongText) return;
                            break;
                    }
                }
            }

            // Implementation of setting TotalClipLength 
            if (list.Count >= TotalClipLength)
            {
                dataDB.Delete(list[list.Count - 1]);
            }

            dataDB.Insert(model);

            if (pushToDatabase)
                Task.Run(async () => { await FirebaseSingleton.GetInstance.AddClip(model.ContentType == ContentType.Text ? model.RawText : null); });          
          
        }

        public void InsertTextClipNoUpdate(string UnEncryptedText)
        {
            InsertContent(CreateTable(UnEncryptedText, ContentTypes.Text), false);
        }

        #endregion

        #region UpdateData

        /// <summary>
        /// This will compare the given text data with the local database table items.
        /// If not exist such item, it will insert the data.
        /// </summary>
        /// <param name="EncryptedDatabaseText">Encrypted Text Data coming straight away from online database.</param>
        /// <param name="invokeOnInserted">Data will be an unencrypted clip data.</param>
        public void CheckDataAndUpdate(string? EncryptedDatabaseText, Action<string>? invokeOnInserted = null)
        {
            if (EncryptedDatabaseText == null) return;

            var decryptedText = EncryptedDatabaseText.DecryptBase64(DatabaseEncryptPassword);

            bool dataExist = false;
            if (IsSecureDB)
                dataExist = dataDB.GetAllData().Exists(c => c.RawText.DecryptBase64(DatabaseEncryptPassword) == decryptedText);
            else
                dataExist = dataDB.GetAllData().Exists(c => c.RawText == decryptedText);
            if (!dataExist)
            {
                // Insert this data without updating online database.
                Debug.WriteLine("Inserted Data");
                InsertTextClipNoUpdate(decryptedText);
                invokeOnInserted?.Invoke(decryptedText);
            }
        }

        #endregion

        #region DeleteData

        public void DeleteClipData(TableCopy model)
        {
            dataDB.Delete(model);

            Task.Run(async () => { await FirebaseSingleton.GetInstance.RemoveClip(model.ContentType == ContentType.Text ? model.RawText : null); });
        }

        public void DeleteAllData()
        {
            dataDB.ClearAll<TableCopy>();

            Task.Run(async () => { await FirebaseSingleton.GetInstance.RemoveAllClip(); });
        }

        #endregion

        #endregion    
    }

}

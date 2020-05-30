using System.Windows;
using System.Windows.Input;
using System.Windows.Interactivity;

namespace Components
{
    public class EventToCommand : TriggerAction<DependencyObject>
    {
        public ICommand Command
        {
            get { return (ICommand)GetValue(CommandProperty); }
            set { SetValue(CommandProperty, value); }
        }

        // Using a DependencyProperty as the backing store for Command. This enables animation, styling, binding, etc...
        public static readonly DependencyProperty CommandProperty =
            DependencyProperty.Register("Command", typeof(ICommand), typeof(EventToCommand), new PropertyMetadata(null));

        protected override void Invoke(object parameter)
        {
            if (Command != null
                && Command.CanExecute(parameter))
            {
                Command.Execute(parameter);
            }
        }
    }
}

using MaterialDesignThemes.Wpf;
using System;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Media.Animation;

namespace Components
{
    public class BaseCard : Card
    {
        public float animationTime = 0.2f;
        public BaseCard()
        {
            Background = "#3F3F46".GetColor();
            this.Visibility = Visibility.Collapsed;
            Loaded += BaseCard_Loaded;
        }
        public async void BaseCard_Loaded(object sender, RoutedEventArgs e)
        {
            await AnimateIn();
        }
        
        public async Task AnimateIn()
        {
            var sb = new Storyboard();
            var slideAnimation = new ThicknessAnimation
            {
                Duration = new Duration(TimeSpan.FromSeconds(animationTime)),
                From = new Thickness(200, 3, -200, 3),
                To = new Thickness(3),
                DecelerationRatio = 0.0f
            };
            Storyboard.SetTargetProperty(slideAnimation, new PropertyPath("Margin"));
            sb.Children.Add(slideAnimation);

            sb.Begin(this);

            this.Visibility = Visibility.Visible;

            await Task.Delay((int)animationTime);

        }
    }
}

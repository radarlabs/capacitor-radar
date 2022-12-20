import './ExploreContainer.css';

interface ContainerProps {
  logs: string
}

const ExploreContainer: React.FC<ContainerProps> = ({ logs }) => {
  return (
    <div className="container">
      <strong>Ready to create an app?</strong>
      <p>Start with Ionic <a target="_blank" rel="noopener noreferrer" href="https://ionicframework.com/docs/components">UI Components</a></p>
      <textarea value={logs}/>
    </div>
  );
};

export default ExploreContainer;

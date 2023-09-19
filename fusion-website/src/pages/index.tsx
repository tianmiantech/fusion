import yayJpg from '../assets/yay.jpg';

export default function HomePage() {
  return (
    <div>
      <h2>index</h2>
      <p>
        <img src={yayJpg} width="388" />
      </p>
      <p>
        To get started, edit <code>pages/index.tsx</code> and save to reload.
      </p>
    </div>
  );
}
